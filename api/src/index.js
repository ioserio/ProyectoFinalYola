import express from 'express';
import dotenv from 'dotenv';
import mysql from 'mysql2/promise';

dotenv.config();

const app = express();
app.use(express.json());

// Config DB
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASS || '',
  database: process.env.DB_NAME || 'u332271143_inventario',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// Health
app.get('/api/health', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1 AS ok');
    res.json({ status: 'ok', db: rows[0].ok === 1 });
  } catch (e) {
    res.status(500).json({ status: 'error', message: e.message });
  }
});

// Categorias
app.post('/api/categorias', async (req, res) => {
  const { nombre, descripcion } = req.body;
  if (!nombre) return res.status(400).json({ error: 'nombre requerido' });
  try {
    const [result] = await pool.execute(
      'INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)',
      [nombre, descripcion || null]
    );
    const [rows] = await pool.execute('SELECT * FROM categorias WHERE id = ?', [result.insertId]);
    res.status(201).json(rows[0]);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});
app.get('/api/categorias', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM categorias ORDER BY nombre');
    res.json(rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// Productos
app.post('/api/productos', async (req, res) => {
  const { sku, nombre, descripcion, precio, activo = 1, categoria_id } = req.body;
  if (!sku || !nombre) return res.status(400).json({ error: 'sku y nombre requeridos' });
  try {
    const [result] = await pool.execute(
      'INSERT INTO productos (sku, nombre, descripcion, precio, activo, categoria_id) VALUES (?, ?, ?, ?, ?, ?)',
      [sku, nombre, descripcion || null, precio ?? 0, activo ? 1 : 0, categoria_id || null]
    );
    const [rows] = await pool.execute('SELECT * FROM productos WHERE id = ?', [result.insertId]);
    res.status(201).json(rows[0]);
  } catch (e) { res.status(500).json({ error: e.message }); }
});
app.get('/api/productos', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM productos ORDER BY nombre');
    res.json(rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// Almacenes
app.post('/api/almacenes', async (req, res) => {
  const { nombre, ubicacion, activo = 1 } = req.body;
  if (!nombre) return res.status(400).json({ error: 'nombre requerido' });
  try {
    const [result] = await pool.execute(
      'INSERT INTO almacenes (nombre, ubicacion, activo) VALUES (?, ?, ?)',
      [nombre, ubicacion || null, activo ? 1 : 0]
    );
    const [rows] = await pool.execute('SELECT * FROM almacenes WHERE id = ?', [result.insertId]);
    res.status(201).json(rows[0]);
  } catch (e) { res.status(500).json({ error: e.message }); }
});
app.get('/api/almacenes', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM almacenes ORDER BY nombre');
    res.json(rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// Inventarios
app.post('/api/inventarios', async (req, res) => {
  const { producto_id, almacen_id, stock = 0, stock_minimo = 0, stock_maximo = null } = req.body;
  if (!producto_id || !almacen_id) return res.status(400).json({ error: 'producto_id y almacen_id requeridos' });
  try {
    const [result] = await pool.execute(
      'INSERT INTO inventarios (producto_id, almacen_id, stock, stock_minimo, stock_maximo) VALUES (?, ?, ?, ?, ?)',
      [producto_id, almacen_id, stock, stock_minimo, stock_maximo]
    );
    const [rows] = await pool.execute('SELECT * FROM inventarios WHERE id = ?', [result.insertId]);
    res.status(201).json(rows[0]);
  } catch (e) {
    if (e.code === 'ER_DUP_ENTRY') return res.status(409).json({ error: 'Inventario ya existe para producto/almacen' });
    res.status(500).json({ error: e.message });
  }
});
app.get('/api/inventarios', async (_req, res) => {
  try {
    const [rows] = await pool.query(
      'SELECT i.*, p.nombre AS producto_nombre, a.nombre AS almacen_nombre FROM inventarios i JOIN productos p ON p.id = i.producto_id JOIN almacenes a ON a.id = i.almacen_id ORDER BY p.nombre, a.nombre'
    );
    res.json(rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// Movimientos
app.post('/api/movimientos', async (req, res) => {
  const { inventario_id, tipo, cantidad, referencia = null, comentario = null } = req.body;
  if (!inventario_id || !tipo || !cantidad) return res.status(400).json({ error: 'inventario_id, tipo y cantidad requeridos' });
  if (!['ENTRADA','SALIDA','AJUSTE'].includes(String(tipo))) return res.status(400).json({ error: 'tipo inválido' });
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    const [[inv]] = await conn.query('SELECT * FROM inventarios WHERE id = ?', [inventario_id]);
    if (!inv) {
      await conn.rollback();
      return res.status(404).json({ error: 'inventario no encontrado' });
    }
    let nuevoStock = inv.stock;
    if (tipo === 'ENTRADA') nuevoStock += cantidad;
    else if (tipo === 'SALIDA') nuevoStock -= cantidad;
    else nuevoStock = cantidad; // AJUSTE

    await conn.execute('UPDATE inventarios SET stock = ? WHERE id = ?', [nuevoStock, inventario_id]);
    const [result] = await conn.execute(
      'INSERT INTO movimientos_inventario (inventario_id, tipo, cantidad, referencia, comentario) VALUES (?, ?, ?, ?, ?)',
      [inventario_id, tipo, cantidad, referencia, comentario]
    );

    const [[mov]] = await conn.query('SELECT * FROM movimientos_inventario WHERE id = ?', [result.insertId]);
    await conn.commit();
    res.status(201).json(mov);
  } catch (e) {
    await conn.rollback();
    res.status(500).json({ error: e.message });
  } finally {
    conn.release();
  }
});
app.get('/api/movimientos', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM movimientos_inventario ORDER BY id DESC');
    res.json(rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

const port = Number(process.env.PORT || 3000);
app.listen(port, () => {
  console.log(`API Inventario escuchando en http://localhost:${port}`);
});

