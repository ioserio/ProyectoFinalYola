<?php
// login.php
// Valida usuario (correo o nombre) y contraseña. Recibe POST: usuario|correo, clave
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
  http_response_code(200);
  exit;
}

require_once __DIR__ . '/conexion.php';

// Aceptar tanto 'usuario' (nombre o correo) como 'correo' para compatibilidad
$usuario_input = null;
if (isset($_POST['usuario'])) $usuario_input = trim($_POST['usuario']);
else if (isset($_POST['correo'])) $usuario_input = trim($_POST['correo']);

$clave = isset($_POST['clave']) ? $_POST['clave'] : '';

if ($usuario_input === null || $usuario_input === '' || $clave === '') {
  http_response_code(400);
  echo json_encode(['success' => false, 'msg' => 'usuario (nombre o correo) y clave son requeridos']);
  exit;
}

try {
  // Detectar si la tabla usuarios tiene columna 'clave_hash' o 'clave'
  $has_clave_hash = false;
  $has_clave_plain = false;

  $res = $mysqli->query("SHOW COLUMNS FROM usuarios LIKE 'clave_hash'");
  if ($res && $res->num_rows > 0) $has_clave_hash = true;
  if ($res) $res->free();

  $res2 = $mysqli->query("SHOW COLUMNS FROM usuarios LIKE 'clave'");
  if ($res2 && $res2->num_rows > 0) $has_clave_plain = true;
  if ($res2) $res2->free();

  // Construir SELECT dinámico según columnas disponibles
  $select_cols = 'id, nombre, rol';
  if ($has_clave_hash) $select_cols .= ', clave_hash';
  if ($has_clave_plain) $select_cols .= ', clave';

  // Buscar por correo OR nombre
  $stmt = $mysqli->prepare("SELECT $select_cols FROM usuarios WHERE correo = ? OR nombre = ? LIMIT 1");
  if (! $stmt) { throw new Exception($mysqli->error); }
  $stmt->bind_param('ss', $usuario_input, $usuario_input);
  $stmt->execute();
  $res3 = $stmt->get_result();
  $row = $res3->fetch_assoc();
  if (! $row) {
    echo json_encode(['success' => false, 'msg' => 'Usuario no encontrado']);
    exit;
  }

  // Validación: si existe clave_hash, primero intentar password_verify.
  $valid = false;
  if ($has_clave_hash && isset($row['clave_hash'])) {
    $stored = $row['clave_hash'];
    // Intentar verificación con password_verify (si fue hasheada)
    if (password_verify($clave, $stored)) {
      $valid = true;
    } else {
      // Fallback de compatibilidad: si el valor almacenado no es un hash y coincide textualmente
      if (hash_equals($stored, $clave)) {
        $valid = true;
      }
    }
  }

  // Si aún no validado y tabla tiene columna 'clave' (texto plano), comparar directamente
  if (! $valid && $has_clave_plain && isset($row['clave'])) {
    if (hash_equals($row['clave'], $clave)) {
      $valid = true;
    }
  }

  if (! $valid) {
    echo json_encode(['success' => false, 'msg' => 'Credenciales inválidas']);
    exit;
  }

  echo json_encode(['success' => true, 'msg' => 'ok', 'id' => (int)$row['id'], 'nombre' => $row['nombre'], 'rol' => $row['rol']]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(['success' => false, 'msg' => $e->getMessage()]);
}

?>
