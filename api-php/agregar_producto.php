<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: *");
header("Access-Control-Allow-Methods: *");

require_once __DIR__ . '/conexion.php';

$sku = isset($_POST['sku']) ? trim($_POST['sku']) : '';
$nombre = isset($_POST['nombre']) ? trim($_POST['nombre']) : '';
$descripcion = isset($_POST['descripcion']) ? trim($_POST['descripcion']) : null;
$precio = isset($_POST['precio']) ? floatval($_POST['precio']) : 0.0;
$categoria_id = isset($_POST['categoria_id']) && $_POST['categoria_id'] !== '' ? intval($_POST['categoria_id']) : null;

if ($sku === '' || $nombre === '') {
  echo json_encode(['success' => false, 'msg' => 'SKU y nombre son requeridos']);
  exit;
}

try {
  if ($categoria_id === null) {
    $stmt = $mysqli->prepare('INSERT INTO productos (sku, nombre, descripcion, precio, activo) VALUES (?, ?, ?, ?, 1)');
    if (!$stmt) { throw new Exception($mysqli->error); }
    $stmt->bind_param('sssd', $sku, $nombre, $descripcion, $precio);
  } else {
    $stmt = $mysqli->prepare('INSERT INTO productos (sku, nombre, descripcion, precio, categoria_id, activo) VALUES (?, ?, ?, ?, ?, 1)');
    if (!$stmt) { throw new Exception($mysqli->error); }
    $stmt->bind_param('sssdi', $sku, $nombre, $descripcion, $precio, $categoria_id);
  }

  if (!$stmt->execute()) {
    // Duplicados o errores SQL
    throw new Exception($stmt->error ?: 'Error al insertar producto');
  }

  echo json_encode(['success' => true, 'msg' => 'ok', 'id' => $stmt->insert_id]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

