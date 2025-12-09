<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: *');

require_once __DIR__ . '/conexion.php';

try {
  $sql = 'SELECT id, nombre, ubicacion FROM almacenes ORDER BY nombre';
  $result = $mysqli->query($sql);
  if (!$result) { throw new Exception($mysqli->error ?: 'Error al listar almacenes'); }

  $rows = [];
  while ($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $rows[] = $row;
  }

  echo json_encode($rows);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}
