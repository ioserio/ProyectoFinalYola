<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: *");
header("Access-Control-Allow-Methods: *");

require_once __DIR__ . '/conexion.php';

$inventario_id = isset($_POST['inventario_id']) ? intval($_POST['inventario_id']) : 0;
$tipo = isset($_POST['tipo']) ? $_POST['tipo'] : '';
$cantidad = isset($_POST['cantidad']) ? intval($_POST['cantidad']) : 0;
$referencia = isset($_POST['referencia']) ? $_POST['referencia'] : null;
$comentario = isset($_POST['comentario']) ? $_POST['comentario'] : null;

if ($inventario_id <= 0 || $tipo === '' || $cantidad < 0) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'inventario_id, tipo y cantidad válidos requeridos']);
  exit;
}

if (!in_array($tipo, ['ENTRADA','SALIDA','AJUSTE'])) {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'tipo inválido']);
  exit;
}

try {
  $mysqli->begin_transaction();

  // Validar inventario
  $stmt0 = $mysqli->prepare('SELECT stock FROM inventarios WHERE id = ? FOR UPDATE');
  if (!$stmt0) { throw new Exception($mysqli->error); }
  $stmt0->bind_param('i', $inventario_id);
  if (!$stmt0->execute()) { throw new Exception($stmt0->error); }
  $res0 = $stmt0->get_result();
  if (!$res0 || !$res0->fetch_assoc()) {
    throw new Exception('inventario no encontrado');
  }
  $res0->data_seek(0);
  $row0 = $res0->fetch_assoc();
  $stock = (int)$row0['stock'];

  // Calcular nuevo stock
  $nuevo = $stock;
  if ($tipo === 'ENTRADA') { $nuevo = $stock + $cantidad; }
  elseif ($tipo === 'SALIDA') { $nuevo = $stock - $cantidad; if ($nuevo < 0) { throw new Exception('stock insuficiente'); } }
  else { $nuevo = $cantidad; } // AJUSTE

  // Actualizar stock
  $stmt1 = $mysqli->prepare('UPDATE inventarios SET stock = ?, actualizado_en = CURRENT_TIMESTAMP WHERE id = ?');
  if (!$stmt1) { throw new Exception($mysqli->error); }
  $stmt1->bind_param('ii', $nuevo, $inventario_id);
  if (!$stmt1->execute()) { throw new Exception($stmt1->error); }

  // Registrar movimiento
  $stmt2 = $mysqli->prepare('INSERT INTO movimientos_inventario (inventario_id, tipo, cantidad, referencia, comentario) VALUES (?, ?, ?, ?, ?)');
  if (!$stmt2) { throw new Exception($mysqli->error); }
  $stmt2->bind_param('isiss', $inventario_id, $tipo, $cantidad, $referencia, $comentario);
  if (!$stmt2->execute()) { throw new Exception($stmt2->error); }

  $mysqli->commit();
  echo json_encode(['success' => true, 'msg' => 'ok', 'id' => $stmt2->insert_id, 'stock' => $nuevo]);
} catch (Exception $e) {
  $mysqli->rollback();
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

