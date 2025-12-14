-- Script de inicialización para PostgreSQL
-- Este script se ejecuta automáticamente cuando se levanta el contenedor Docker

-- Crear las secuencias
CREATE SEQUENCE IF NOT EXISTS public.equipos_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.etiquetas_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.tareas_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.usuarios_id_seq START WITH 1 INCREMENT BY 1;

-- Crear tabla usuarios
CREATE TABLE IF NOT EXISTS public.usuarios (
    id bigint PRIMARY KEY DEFAULT nextval('public.usuarios_id_seq'::regclass),
    admin boolean,
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);

-- Crear tabla equipos
CREATE TABLE IF NOT EXISTS public.equipos (
    id bigint PRIMARY KEY DEFAULT nextval('public.equipos_id_seq'::regclass),
    nombre character varying(255) NOT NULL
);

-- Crear tabla equipo_usuario (relación muchos a muchos)
CREATE TABLE IF NOT EXISTS public.equipo_usuario (
    fk_equipo bigint NOT NULL,
    fk_usuario bigint NOT NULL,
    PRIMARY KEY (fk_equipo, fk_usuario),
    FOREIGN KEY (fk_equipo) REFERENCES public.equipos(id),
    FOREIGN KEY (fk_usuario) REFERENCES public.usuarios(id)
);

-- Crear tabla etiquetas
CREATE TABLE IF NOT EXISTS public.etiquetas (
    id bigint PRIMARY KEY DEFAULT nextval('public.etiquetas_id_seq'::regclass),
    nombre character varying(255) NOT NULL,
    color character varying(255) NOT NULL,
    usuario_id bigint NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE
);

-- Crear tabla tareas
CREATE TABLE IF NOT EXISTS public.tareas (
    id bigint PRIMARY KEY DEFAULT nextval('public.tareas_id_seq'::regclass),
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL,
    descripcion character varying(255),
    position integer,
    fecha_finalizacion date,
    visible boolean NOT NULL DEFAULT true,
    completada boolean NOT NULL DEFAULT false,
    tarea_padre_id bigint,
    FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id),
    FOREIGN KEY (tarea_padre_id) REFERENCES public.tareas(id) ON DELETE CASCADE
);

-- Crear tabla tarea_etiquetas (relación muchos a muchos)
CREATE TABLE IF NOT EXISTS public.tarea_etiquetas (
    tarea_id bigint NOT NULL,
    etiqueta_id bigint NOT NULL,
    PRIMARY KEY (tarea_id, etiqueta_id),
    FOREIGN KEY (tarea_id) REFERENCES public.tareas(id) ON DELETE CASCADE,
    FOREIGN KEY (etiqueta_id) REFERENCES public.etiquetas(id) ON DELETE CASCADE
);

-- Asignar ownership a usuario mads
ALTER TABLE public.usuarios OWNER TO mads;
ALTER TABLE public.equipos OWNER TO mads;
ALTER TABLE public.equipo_usuario OWNER TO mads;
ALTER TABLE public.etiquetas OWNER TO mads;
ALTER TABLE public.tareas OWNER TO mads;
ALTER TABLE public.tarea_etiquetas OWNER TO mads;
ALTER SEQUENCE public.equipos_id_seq OWNER TO mads;
ALTER SEQUENCE public.etiquetas_id_seq OWNER TO mads;
ALTER SEQUENCE public.tareas_id_seq OWNER TO mads;
ALTER SEQUENCE public.usuarios_id_seq OWNER TO mads;

