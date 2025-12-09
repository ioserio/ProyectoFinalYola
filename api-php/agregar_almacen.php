<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: *');

require_once __DIR__ . '/conexion.php';

$nombre = isset($_POST['nombre']) ? trim($_POST['nombre']) : '';
$ubicacion = isset($_POST['ubicacion']) ? trim($_POST['ubicacion']) : null;

if ($nombre === '') {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'Nombre requerido']);
  exit;
}

try {
  $stmt = $mysqli->prepare('INSERT INTO almacenes (nombre, ubicacion) VALUES (?, ?)');
  if (!$stmt) { throw new Exception($mysqli->error ?: 'Error al preparar sentencia'); }
  $stmt->bind_param('ss', $nombre, $ubicacion);
  if (!$stmt->execute()) { throw new Exception($stmt->error ?: 'Error al crear almacén'); }
  echo json_encode(['success' => true, 'msg' => 'ok', 'id' => $stmt->insert_id]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

