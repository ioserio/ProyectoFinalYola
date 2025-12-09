<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: *');

require_once __DIR__ . '/conexion.php';

try {
  $sql = "SELECT inv.id,
                 inv.producto_id,
                 p.sku,
                 p.nombre AS nombre_producto,
                 inv.almacen_id,
                 a.nombre AS nombre_almacen,
                 inv.stock
          FROM inventarios inv
          JOIN productos p ON p.id = inv.producto_id
          JOIN almacenes a ON a.id = inv.almacen_id
          ORDER BY p.nombre, a.nombre";
  $result = $mysqli->query($sql);
  if (!$result) { throw new Exception($mysqli->error ?: 'Error al listar inventarios'); }

  $rows = [];
  while ($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['producto_id'] = (int)$row['producto_id'];
    $row['almacen_id'] = (int)$row['almacen_id'];
    $row['stock'] = (int)$row['stock'];
    $rows[] = $row;
  }

  echo json_encode($rows);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

