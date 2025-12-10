<?php
// registrar_usuario.php
// Crea un usuario en la tabla `usuarios`. Recibe POST: nombre, correo, clave, rol (opcional)
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
  http_response_code(200);
  exit;
}

require_once __DIR__ . '/conexion.php';

$nombre = isset($_POST['nombre']) ? trim($_POST['nombre']) : '';
$correo = isset($_POST['correo']) ? trim($_POST['correo']) : '';
$clave = isset($_POST['clave']) ? $_POST['clave'] : '';
$rol = isset($_POST['rol']) ? trim($_POST['rol']) : 'user';

if ($nombre === '' || $correo === '' || $clave === '') {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'nombre, correo y clave son requeridos']);
  exit;
}

try {
  // Verificar que no exista el correo
  $stmt = $mysqli->prepare('SELECT id FROM usuarios WHERE correo = ? LIMIT 1');
  $stmt->bind_param('s', $correo);
  $stmt->execute();
  $res = $stmt->get_result();
  if ($res->fetch_assoc()) {
    echo json_encode(['success' => false, 'msg' => 'El correo ya está registrado']);
    exit;
  }
  $res->free();

  $clave_hash = password_hash($clave, PASSWORD_DEFAULT);
  $stmt2 = $mysqli->prepare('INSERT INTO usuarios (nombre, correo, clave_hash, rol) VALUES (?, ?, ?, ?)');
  if (! $stmt2) { throw new Exception($mysqli->error); }
  $stmt2->bind_param('ssss', $nombre, $correo, $clave_hash, $rol);
  if (! $stmt2->execute()) { throw new Exception($stmt2->error ?: 'Error al insertar usuario'); }

  echo json_encode(['success' => true, 'msg' => 'ok', 'id' => $stmt2->insert_id]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

?>
