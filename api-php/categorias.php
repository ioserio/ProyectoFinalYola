<?php
require_once __DIR__.'/conexion.php';

// Manejo de POST para agregar
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  header('Content-Type: application/json');
  $nombre = isset($_POST['nombre']) ? trim($_POST['nombre']) : '';
  $descripcion = isset($_POST['descripcion']) ? trim($_POST['descripcion']) : null;
  if ($nombre === '') { http_response_code(400); echo json_encode(['success'=>false,'msg'=>'Nombre requerido']); exit; }
  try {
    $stmt = $mysqli->prepare('INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)');
    if (!$stmt) throw new Exception($mysqli->error);
    $stmt->bind_param('ss', $nombre, $descripcion);
    if (!$stmt->execute()) throw new Exception($stmt->error);
    echo json_encode(['success'=>true,'msg'=>'ok','id'=>$stmt->insert_id]);
  } catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success'=>false,'msg'=>$e->getMessage()]);
  }
  exit;
}

// Página HTML con listado y formulario
?><!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Categorías</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 0; padding: 24px; background: #f6f7fb; }
    h1 { margin: 0 0 16px; }
    .wrap { display: grid; grid-template-columns: 1fr 320px; gap: 24px; }
    table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
    th, td { padding: 10px 12px; border-bottom: 1px solid #eee; }
    th { text-align: left; background: #fafafa; }
    form { background: #fff; padding: 16px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
    input, textarea { width: 100%; padding: 10px; margin: 6px 0 12px; border: 1px solid #ddd; border-radius: 6px; }
    button { padding: 10px 14px; background: #2d6cdf; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
    .muted { color: #666; font-size: 14px; }
  </style>
</head>
<body>
  <h1>Categorías</h1>
  <p class="muted">Administra las categorías registradas</p>
  <div class="wrap">
    <div>
      <table>
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Descripción</th></tr>
        </thead>
        <tbody>
          <?php
          try {
            $res = $mysqli->query('SELECT id, nombre, descripcion FROM categorias ORDER BY nombre');
            if ($res) {
              while ($row = $res->fetch_assoc()) {
                echo '<tr>';
                echo '<td>'.htmlspecialchars($row['id']).'</td>';
                echo '<td>'.htmlspecialchars($row['nombre']).'</td>';
                echo '<td>'.htmlspecialchars($row['descripcion'] ?? '').'</td>';
                echo '</tr>';
              }
            }
          } catch (Exception $e) {
            echo '<tr><td colspan="3">Error: '.htmlspecialchars($e->getMessage()).'</td></tr>';
          }
          ?>
        </tbody>
      </table>
    </div>
    <div>
      <form id="frm" method="post" action="categorias.php" onsubmit="return enviar(event)">
        <h3>Nueva categoría</h3>
        <label>Nombre</label>
        <input type="text" name="nombre" required>
        <label>Descripción</label>
        <textarea name="descripcion" rows="3"></textarea>
        <button type="submit">Guardar</button>
        <p id="msg" class="muted"></p>
      </form>
    </div>
  </div>

  <script>
    async function enviar(e) {
      e.preventDefault();
      const form = document.getElementById('frm');
      const data = new FormData(form);
      const res = await fetch('categorias.php', { method: 'POST', body: data });
      const json = await res.json();
      const msg = document.getElementById('msg');
      msg.textContent = json.msg || (json.success ? 'ok' : 'Error');
      if (json.success) { location.reload(); }
      return false;
    }
  </script>
</body>
</html>

