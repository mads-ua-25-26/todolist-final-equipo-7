-- Script de datos de prueba SIMPLIFICADO para la base de datos mads
-- Este script inserta datos básicos sin usar bloques DO complejos

-- Limpiar datos existentes
TRUNCATE TABLE tarea_etiquetas, equipo_usuario, tareas, etiquetas, equipos, usuarios RESTART IDENTITY CASCADE;

-- =====================================================
-- USUARIOS (5 usuarios de prueba)
-- =====================================================
INSERT INTO usuarios (email, nombre, password, fecha_nacimiento, admin) VALUES
('admin@mads.com', 'Administrador', '123', '1990-01-15', true),
('juan.perez@email.com', 'Juan Pérez', '123', '1995-05-20', false),
('maria.garcia@email.com', 'María García', '123', '1992-08-10', false),
('carlos.lopez@email.com', 'Carlos López', '123', '1988-03-25', false),
('ana.martinez@email.com', 'Ana Martínez', '123', '1997-11-30', false);

-- =====================================================
-- EQUIPOS (3 equipos)
-- =====================================================
INSERT INTO equipos (nombre) VALUES
('Desarrollo Frontend'),
('Desarrollo Backend'),
('Equipo de Diseño');

-- =====================================================
-- RELACIÓN EQUIPOS-USUARIOS
-- =====================================================
INSERT INTO equipo_usuario (fk_equipo, fk_usuario)
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Desarrollo Frontend' AND u.email = 'juan.perez@email.com'
UNION ALL
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Desarrollo Frontend' AND u.email = 'ana.martinez@email.com'
UNION ALL
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Desarrollo Backend' AND u.email = 'juan.perez@email.com'
UNION ALL
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Desarrollo Backend' AND u.email = 'maria.garcia@email.com'
UNION ALL
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Desarrollo Backend' AND u.email = 'carlos.lopez@email.com'
UNION ALL
SELECT e.id, u.id FROM equipos e, usuarios u
WHERE e.nombre = 'Equipo de Diseño' AND u.email = 'ana.martinez@email.com';

-- =====================================================
-- ETIQUETAS (15 etiquetas, 3 por usuario)
-- =====================================================
-- Admin
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Urgente', '#FF0000', id FROM usuarios WHERE email = 'admin@mads.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Importante', '#FF8800', id FROM usuarios WHERE email = 'admin@mads.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Baja prioridad', '#00FF00', id FROM usuarios WHERE email = 'admin@mads.com';

-- Juan
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Trabajo', '#0088FF', id FROM usuarios WHERE email = 'juan.perez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Personal', '#8800FF', id FROM usuarios WHERE email = 'juan.perez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Estudio', '#00FFFF', id FROM usuarios WHERE email = 'juan.perez@email.com';

-- María
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Frontend', '#FF1493', id FROM usuarios WHERE email = 'maria.garcia@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Backend', '#4169E1', id FROM usuarios WHERE email = 'maria.garcia@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Bug', '#DC143C', id FROM usuarios WHERE email = 'maria.garcia@email.com';

-- Carlos
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Reunión', '#FFD700', id FROM usuarios WHERE email = 'carlos.lopez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Código', '#32CD32', id FROM usuarios WHERE email = 'carlos.lopez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Documentación', '#FF6347', id FROM usuarios WHERE email = 'carlos.lopez@email.com';

-- Ana
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Diseño', '#FF69B4', id FROM usuarios WHERE email = 'ana.martinez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'Prototipo', '#9370DB', id FROM usuarios WHERE email = 'ana.martinez@email.com';
INSERT INTO etiquetas (nombre, color, usuario_id)
SELECT 'UX', '#20B2AA', id FROM usuarios WHERE email = 'ana.martinez@email.com';

-- =====================================================
-- TAREAS PRINCIPALES
-- =====================================================
-- Admin
INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Revisar informes mensuales', 'Revisar todos los informes del mes', id, 1, '2025-12-20', true, false
FROM usuarios WHERE email = 'admin@mads.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Planificación Q1 2026', 'Planificar objetivos del trimestre', id, 2, '2025-12-31', true, false
FROM usuarios WHERE email = 'admin@mads.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Reunión con stakeholders', 'Presentar resultados del proyecto', id, 3, '2025-12-18', true, true
FROM usuarios WHERE email = 'admin@mads.com';

-- Juan
INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Desarrollar módulo de autenticación', 'Implementar login y registro', id, 1, '2025-12-22', true, false
FROM usuarios WHERE email = 'juan.perez@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Optimizar consultas SQL', 'Mejorar rendimiento de queries', id, 2, '2025-12-25', true, false
FROM usuarios WHERE email = 'juan.perez@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Actualizar dependencias', 'Actualizar librerías a últimas versiones', id, 3, '2025-12-30', true, true
FROM usuarios WHERE email = 'juan.perez@email.com';

-- María
INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Diseñar arquitectura microservicios', 'Definir estructura de servicios', id, 1, '2025-12-28', true, false
FROM usuarios WHERE email = 'maria.garcia@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Implementar API Gateway', 'Configurar gateway para microservicios', id, 2, '2026-01-10', true, false
FROM usuarios WHERE email = 'maria.garcia@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Corregir bug en producción', 'Error en cálculo de totales', id, 3, '2025-12-15', true, true
FROM usuarios WHERE email = 'maria.garcia@email.com';

-- Carlos
INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Migrar base de datos', 'Migrar de MySQL a PostgreSQL', id, 1, '2026-01-15', true, false
FROM usuarios WHERE email = 'carlos.lopez@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Implementar cache Redis', 'Añadir capa de cache', id, 2, '2026-01-20', true, false
FROM usuarios WHERE email = 'carlos.lopez@email.com';

-- Ana
INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Rediseñar interfaz principal', 'Nuevo diseño responsive', id, 1, '2025-12-23', true, false
FROM usuarios WHERE email = 'ana.martinez@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Crear sistema de componentes', 'Design system reutilizable', id, 2, '2026-01-08', true, false
FROM usuarios WHERE email = 'ana.martinez@email.com';

INSERT INTO tareas (titulo, descripcion, usuario_id, position, fecha_finalizacion, visible, completada)
SELECT 'Actualizar guía de estilo', 'Documentar componentes', id, 3, '2025-12-27', true, true
FROM usuarios WHERE email = 'ana.martinez@email.com';

-- =====================================================
-- SUBTAREAS
-- =====================================================
-- Subtareas de Juan (Desarrollar módulo de autenticación)
INSERT INTO tareas (titulo, descripcion, usuario_id, tarea_padre_id, visible, completada)
SELECT 'Crear formulario de login', 'HTML + CSS del formulario', u.id, t.id, true, true
FROM usuarios u, tareas t
WHERE u.email = 'juan.perez@email.com'
  AND t.titulo = 'Desarrollar módulo de autenticación'
  AND t.usuario_id = u.id;

INSERT INTO tareas (titulo, descripcion, usuario_id, tarea_padre_id, visible, completada)
SELECT 'Implementar validación', 'Validar campos del formulario', u.id, t.id, true, false
FROM usuarios u, tareas t
WHERE u.email = 'juan.perez@email.com'
  AND t.titulo = 'Desarrollar módulo de autenticación'
  AND t.usuario_id = u.id;

-- Subtareas de Carlos (Migrar base de datos)
INSERT INTO tareas (titulo, descripcion, usuario_id, tarea_padre_id, visible, completada)
SELECT 'Backup de datos actuales', 'Hacer copia de seguridad completa', u.id, t.id, true, true
FROM usuarios u, tareas t
WHERE u.email = 'carlos.lopez@email.com'
  AND t.titulo = 'Migrar base de datos'
  AND t.usuario_id = u.id;

INSERT INTO tareas (titulo, descripcion, usuario_id, tarea_padre_id, visible, completada)
SELECT 'Crear scripts de migración', 'Scripts SQL para transferir datos', u.id, t.id, true, false
FROM usuarios u, tareas t
WHERE u.email = 'carlos.lopez@email.com'
  AND t.titulo = 'Migrar base de datos'
  AND t.usuario_id = u.id;

-- =====================================================
-- RELACIÓN TAREAS-ETIQUETAS
-- =====================================================
-- Admin: Revisar informes -> Urgente, Importante
INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Revisar informes mensuales' AND t.usuario_id = u.id
  AND e.nombre = 'Urgente' AND e.usuario_id = u.id
  AND u.email = 'admin@mads.com';

INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Revisar informes mensuales' AND t.usuario_id = u.id
  AND e.nombre = 'Importante' AND e.usuario_id = u.id
  AND u.email = 'admin@mads.com';

-- Juan: Desarrollar autenticación -> Trabajo
INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Desarrollar módulo de autenticación' AND t.usuario_id = u.id
  AND e.nombre = 'Trabajo' AND e.usuario_id = u.id
  AND u.email = 'juan.perez@email.com';

-- María: Bug -> Bug
INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Corregir bug en producción' AND t.usuario_id = u.id
  AND e.nombre = 'Bug' AND e.usuario_id = u.id
  AND u.email = 'maria.garcia@email.com';

-- Carlos: Migrar BD -> Código
INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Migrar base de datos' AND t.usuario_id = u.id
  AND e.nombre = 'Código' AND e.usuario_id = u.id
  AND u.email = 'carlos.lopez@email.com';

-- Ana: Rediseñar -> Diseño
INSERT INTO tarea_etiquetas (tarea_id, etiqueta_id)
SELECT t.id, e.id FROM tareas t, etiquetas e, usuarios u
WHERE t.titulo = 'Rediseñar interfaz principal' AND t.usuario_id = u.id
  AND e.nombre = 'Diseño' AND e.usuario_id = u.id
  AND u.email = 'ana.martinez@email.com';

-- =====================================================
-- VERIFICAR RESULTADOS
-- =====================================================
SELECT '=====================================' as info;
SELECT 'RESUMEN DE DATOS INSERTADOS' as info;
SELECT '=====================================' as info;
SELECT 'Usuarios' as tipo, COUNT(*) as total FROM usuarios
UNION ALL SELECT 'Equipos', COUNT(*) FROM equipos
UNION ALL SELECT 'Etiquetas', COUNT(*) FROM etiquetas
UNION ALL SELECT 'Tareas principales', COUNT(*) FROM tareas WHERE tarea_padre_id IS NULL
UNION ALL SELECT 'Subtareas', COUNT(*) FROM tareas WHERE tarea_padre_id IS NOT NULL
UNION ALL SELECT 'Total tareas', COUNT(*) FROM tareas
UNION ALL SELECT 'Relaciones equipo-usuario', COUNT(*) FROM equipo_usuario
UNION ALL SELECT 'Relaciones tarea-etiqueta', COUNT(*) FROM tarea_etiquetas;
SELECT '=====================================' as info;
SELECT 'CARGA COMPLETADA!' as info;

