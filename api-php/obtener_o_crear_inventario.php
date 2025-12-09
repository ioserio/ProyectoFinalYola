<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: *");
header("Access-Control-Allow-Methods: *");

require_once __DIR__ . '/conexion.php';

$producto_id = isset($_POST['producto_id']) ? intval($_POST['producto_id']) : 0;
$almacen_id = isset($_POST['almacen_id']) ? intval($_POST['almacen_id']) : 0;

if ($producto_id <= 0 || $almacen_id <= 0) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'producto_id y almacen_id requeridos']);
  exit;
}

try {
  // Buscar inventario existente
  $stmt = $mysqli->prepare('SELECT id FROM inventarios WHERE producto_id = ? AND almacen_id = ?');
  if (!$stmt) { throw new Exception($mysqli->error); }
  $stmt->bind_param('ii', $producto_id, $almacen_id);
  if (!$stmt->execute()) { throw new Exception($stmt->error); }
  $res = $stmt->get_result();
  if ($row = $res->fetch_assoc()) {
    echo json_encode(['success' => true, 'inventario_id' => (int)$row['id']]);
    exit;
  }

  // Crear inventario nuevo
  $stmt2 = $mysqli->prepare('INSERT INTO inventarios (producto_id, almacen_id, stock, stock_minimo) VALUES (?, ?, 0, 0)');
  if (!$stmt2) { throw new Exception($mysqli->error); }
  $stmt2->bind_param('ii', $producto_id, $almacen_id);
  if (!$stmt2->execute()) { throw new Exception($stmt2->error); }
  echo json_encode(['success' => true, 'inventario_id' => $stmt2->insert_id]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

