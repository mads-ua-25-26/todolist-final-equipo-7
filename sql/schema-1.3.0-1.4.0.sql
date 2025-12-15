-- Script de migración de schema 1.3.0 a 1.4.0
-- Añade funcionalidad de etiquetas y nuevos campos para tareas

-- Crear tabla de etiquetas
CREATE TABLE public.etiquetas (
    id bigint NOT NULL,
    nombre character varying(255) NOT NULL,
    color character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);

ALTER TABLE public.etiquetas OWNER TO mads;

CREATE SEQUENCE public.etiquetas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.etiquetas_id_seq OWNER TO mads;
ALTER SEQUENCE public.etiquetas_id_seq OWNED BY public.etiquetas.id;
ALTER TABLE ONLY public.etiquetas ALTER COLUMN id SET DEFAULT nextval('public.etiquetas_id_seq'::regclass);

-- Crear tabla de relación tarea-etiquetas
CREATE TABLE public.tarea_etiquetas (
    tarea_id bigint NOT NULL,
    etiqueta_id bigint NOT NULL
);

ALTER TABLE public.tarea_etiquetas OWNER TO mads;

-- Añadir nuevas columnas a tareas
ALTER TABLE public.tareas
ADD COLUMN position integer,
ADD COLUMN fecha_finalizacion date,
ADD COLUMN visible boolean NOT NULL DEFAULT true,
ADD COLUMN completada boolean NOT NULL DEFAULT false,
ADD COLUMN tarea_padre_id bigint;

-- Añadir constraints
ALTER TABLE ONLY public.etiquetas
    ADD CONSTRAINT etiquetas_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tarea_etiquetas
    ADD CONSTRAINT tarea_etiquetas_pkey PRIMARY KEY (tarea_id, etiqueta_id);

-- Añadir foreign keys
ALTER TABLE ONLY public.etiquetas
    ADD CONSTRAINT fk_etiquetas_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);

ALTER TABLE ONLY public.tarea_etiquetas
    ADD CONSTRAINT fk_tarea_etiquetas_tarea FOREIGN KEY (tarea_id) REFERENCES public.tareas(id);

ALTER TABLE ONLY public.tarea_etiquetas
    ADD CONSTRAINT fk_tarea_etiquetas_etiqueta FOREIGN KEY (etiqueta_id) REFERENCES public.etiquetas(id);

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fk_tareas_tarea_padre FOREIGN KEY (tarea_padre_id) REFERENCES public.tareas(id);

