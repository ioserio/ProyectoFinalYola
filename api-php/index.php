<?php
// Simple dashboard con íconos
?><!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Panel API</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 0; padding: 24px; background: #f6f7fb; }
    h1 { margin: 0 0 16px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; }
    .card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 2px 10px rgba(0,0,0,.08); text-align: center; }
    .card a { text-decoration: none; color: #222; display: block; }
    .icon { font-size: 42px; margin-bottom: 12px; }
    .muted { color: #666; font-size: 14px; }
  </style>
</head>
<body>
  <h1>Panel</h1>
  <p class="muted">Accesos rápidos</p>
  <div class="grid">
    <div class="card">
      <a href="categorias.php">
        <div class="icon">📂</div>
        <div><strong>Categorías</strong></div>
        <div class="muted">Listar y agregar nuevas</div>
      </a>
    </div>
    <div class="card">
      <a href="productos.php">
        <div class="icon">🧾</div>
        <div><strong>Productos</strong></div>
        <div class="muted">Listado (demo)</div>
      </a>
    </div>
  </div>
</body>
</html>

