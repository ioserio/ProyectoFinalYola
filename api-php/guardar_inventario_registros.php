<?php
// guardar_inventario_registros.php
// Recibe JSON: { inventario_ref: string (opcional), almacen_id: int (opcional), items: [ { producto_id: int, conteo: int, sku?:string, descripcion?:string, stock_real?:int } ] }
// Inserta filas en la tabla `inventarios_registros` en una transacción.

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
  // CORS preflight
  http_response_code(200);
  exit;
}

require_once __DIR__ . '/conexion.php';

$body = file_get_contents('php://input');
if (!$body) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'Cuerpo vacío']);
  exit;
}

$data = json_decode($body, true);
if ($data === null) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'JSON inválido']);
  exit;
}

$inventario_ref = isset($data['inventario_ref']) ? trim($data['inventario_ref']) : null;
$almacen_id = isset($data['almacen_id']) && $data['almacen_id'] !== '' ? intval($data['almacen_id']) : null;
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];

if (count($items) === 0) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'No se enviaron items']);
  exit;
}

try {
  // Iniciar transacción
  $mysqli->begin_transaction();

  // Preparar sentencias reutilizables
  $stmtProducto = $mysqli->prepare('SELECT sku, nombre FROM productos WHERE id = ? LIMIT 1');
  if (! $stmtProducto) { throw new Exception('Error preparando consulta productos: ' . $mysqli->error); }

  // Obtener stock desde tabla inventarios (si existe), con o sin almacen_id
  if ($almacen_id !== null) {
    $stmtStock = $mysqli->prepare('SELECT stock FROM inventarios WHERE producto_id = ? AND almacen_id = ? LIMIT 1');
  } else {
    $stmtStock = $mysqli->prepare('SELECT stock FROM inventarios WHERE producto_id = ? LIMIT 1');
  }
  if (! $stmtStock) { throw new Exception('Error preparando consulta stock: ' . $mysqli->error); }

  $stmtInsert = $mysqli->prepare('INSERT INTO inventarios_registros (producto_id, sku, descripcion, stock_real, conteo, inventario_ref) VALUES (?, ?, ?, ?, ?, ?)');
  if (! $stmtInsert) { throw new Exception('Error preparando insert: ' . $mysqli->error); }

  $inserted_ids = [];
  $count = 0;

  foreach ($items as $it) {
    $producto_id = isset($it['producto_id']) ? intval($it['producto_id']) : 0;
    $conteo = isset($it['conteo']) ? intval($it['conteo']) : null;

    if ($producto_id <= 0 || $conteo === null) {
      throw new Exception('Cada item debe incluir producto_id y conteo válidos');
    }

    $sku = isset($it['sku']) ? trim($it['sku']) : null;
    $descripcion = isset($it['descripcion']) ? trim($it['descripcion']) : null;
    $stock_real = isset($it['stock_real']) && $it['stock_real'] !== '' ? intval($it['stock_real']) : null;

    // Si falta sku o descripcion, buscar en productos
    if ($sku === null || $descripcion === null) {
      $stmtProducto->bind_param('i', $producto_id);
      if (! $stmtProducto->execute()) { throw new Exception('Error ejecutando consulta productos: ' . $stmtProducto->error); }
      $res = $stmtProducto->get_result();
      if ($row = $res->fetch_assoc()) {
        if ($sku === null) $sku = $row['sku'];
        if ($descripcion === null) $descripcion = $row['nombre'];
      } else {
        throw new Exception('Producto no encontrado: ' . $producto_id);
      }
      $res->free();
    }

    // Si falta stock_real, intentar obtenerlo desde tabla inventarios
    if ($stock_real === null) {
      if ($almacen_id !== null) {
        $stmtStock->bind_param('ii', $producto_id, $almacen_id);
      } else {
        $stmtStock->bind_param('i', $producto_id);
      }
      if (! $stmtStock->execute()) { throw new Exception('Error ejecutando consulta stock: ' . $stmtStock->error); }
      $resS = $stmtStock->get_result();
      if ($rowS = $resS->fetch_assoc()) {
        $stock_real = intval($rowS['stock']);
      } else {
        // Si no existe un registro en inventarios, asumimos 0
        $stock_real = 0;
      }
      $resS->free();
    }

    // Ahora insertar
    $stmtInsert->bind_param('issiis', $producto_id, $sku, $descripcion, $stock_real, $conteo, $inventario_ref);
    if (! $stmtInsert->execute()) { throw new Exception('Error insertando item: ' . $stmtInsert->error); }

    $inserted_ids[] = $stmtInsert->insert_id;
    $count++;
  }

  // Commit
  $mysqli->commit();

  echo json_encode(['success' => true, 'inserted' => $count, 'ids' => $inserted_ids]);
  exit;
} catch (Exception $e) {
  // Rollback si se inició la transacción
  if ($mysqli->errno) {
    $mysqli->rollback();
  } else {
    // en caso de que begin_transaction fallara, no hacer rollback
  }

  http_response_code(500);
  error_log('Error guardar_inventario_registros: ' . $e->getMessage());
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
  exit;
}

?>
