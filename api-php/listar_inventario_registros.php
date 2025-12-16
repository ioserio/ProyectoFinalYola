<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: *');

require_once __DIR__ . '/conexion.php';

try {
  $sql = "SELECT id,
                 producto_id,
                 sku,
                 descripcion,
                 stock_real,
                 conteo,
                 (conteo - stock_real) AS diferencia,
                 inventario_ref,
                 creado_en
          FROM inventarios_registros
          ORDER BY creado_en DESC, id DESC";
  $result = $mysqli->query($sql);
  if (!$result) { throw new Exception($mysqli->error ?: 'Error al listar inventarios_registros'); }

  $rows = [];
  while ($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['producto_id'] = (int)$row['producto_id'];
    $row['stock_real'] = (int)$row['stock_real'];
    $row['conteo'] = (int)$row['conteo'];
    $row['diferencia'] = (int)$row['diferencia'];
    $rows[] = $row;
  }

  echo json_encode($rows);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

