<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: *');

require_once __DIR__ . '/conexion.php';

// Espera: almacen_id, referencia, comentario, items (JSON array [{producto_id,cantidad,comentario?}])
$almacen_id = isset($_POST['almacen_id']) ? intval($_POST['almacen_id']) : 0;
$referencia = isset($_POST['referencia']) ? trim($_POST['referencia']) : null;
$comentario = isset($_POST['comentario']) ? trim($_POST['comentario']) : null;
$items_json = isset($_POST['items']) ? $_POST['items'] : '[]';

if ($almacen_id <= 0) {
  http_response_code(400);
  echo json_encode(['success'=>false,'msg'=>'almacen_id requerido']);
  exit;
}

$items = json_decode($items_json, true);
if (!is_array($items) || count($items) === 0) {
  http_response_code(400);
  echo json_encode(['success'=>false,'msg'=>'items inválidos']);
  exit;
}

try {
  $mysqli->begin_transaction();

  // Insertar cabecera del ingreso
  $stmtIng = $mysqli->prepare('INSERT INTO ingresos (referencia, comentario, almacen_id) VALUES (?, ?, ?)');
  if (!$stmtIng) throw new Exception($mysqli->error);
  $stmtIng->bind_param('ssi', $referencia, $comentario, $almacen_id);
  if (!$stmtIng->execute()) throw new Exception($stmtIng->error);
  $ingreso_id = $stmtIng->insert_id;

  // Preparar statements reutilizables
  $stmtItem = $mysqli->prepare('INSERT INTO ingreso_items (ingreso_id, producto_id, cantidad, comentario) VALUES (?, ?, ?, ?)');
  if (!$stmtItem) throw new Exception($mysqli->error);

  $stmtSelInv = $mysqli->prepare('SELECT id, stock FROM inventarios WHERE producto_id = ? AND almacen_id = ? FOR UPDATE');
  if (!$stmtSelInv) throw new Exception($mysqli->error);

  $stmtInsInv = $mysqli->prepare('INSERT INTO inventarios (producto_id, almacen_id, stock, stock_minimo) VALUES (?, ?, 0, 0)');
  if (!$stmtInsInv) throw new Exception($mysqli->error);

  $stmtUpdInv = $mysqli->prepare('UPDATE inventarios SET stock = stock + ?, actualizado_en = CURRENT_TIMESTAMP WHERE id = ?');
  if (!$stmtUpdInv) throw new Exception($mysqli->error);

  $stmtMov = $mysqli->prepare('INSERT INTO movimientos_inventario (inventario_id, tipo, cantidad, referencia, comentario) VALUES (?, "ENTRADA", ?, ?, ?)');
  if (!$stmtMov) throw new Exception($mysqli->error);

  $total_lineas = 0;
  foreach ($items as $it) {
    $producto_id = isset($it['producto_id']) ? intval($it['producto_id']) : 0;
    $cantidad = isset($it['cantidad']) ? intval($it['cantidad']) : 0;
    $coment_item = isset($it['comentario']) ? trim($it['comentario']) : null;
    if ($producto_id <= 0 || $cantidad <= 0) continue;

    // Insertar detalle
    $stmtItem->bind_param('iiis', $ingreso_id, $producto_id, $cantidad, $coment_item);
    if (!$stmtItem->execute()) throw new Exception($stmtItem->error);

    // Obtener/crear inventario
    $stmtSelInv->bind_param('ii', $producto_id, $almacen_id);
    if (!$stmtSelInv->execute()) throw new Exception($stmtSelInv->error);
    $resInv = $stmtSelInv->get_result();
    $inventario_id = null;
    if ($row = $resInv->fetch_assoc()) {
      $inventario_id = intval($row['id']);
    } else {
      $stmtInsInv->bind_param('ii', $producto_id, $almacen_id);
      if (!$stmtInsInv->execute()) throw new Exception($stmtInsInv->error);
      $inventario_id = $stmtInsInv->insert_id;
    }

    // Actualizar stock
    $stmtUpdInv->bind_param('ii', $cantidad, $inventario_id);
    if (!$stmtUpdInv->execute()) throw new Exception($stmtUpdInv->error);

    // Registrar movimiento
    $stmtMov->bind_param('iiss', $inventario_id, $cantidad, $referencia, $coment_item);
    if (!$stmtMov->execute()) throw new Exception($stmtMov->error);

    $total_lineas++;
  }

  if ($total_lineas === 0) throw new Exception('No hay líneas válidas para registrar');

  $mysqli->commit();
  echo json_encode(['success'=>true,'msg'=>'ok','ingreso_id'=>$ingreso_id,'lineas'=>$total_lineas]);
} catch (Exception $e) {
  $mysqli->rollback();
  http_response_code(500);
  echo json_encode(['success'=>false,'msg'=>$e->getMessage()]);
}

