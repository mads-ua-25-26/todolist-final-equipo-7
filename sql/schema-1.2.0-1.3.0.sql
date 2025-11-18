--- sql/schema-1.2.0.sql	2025-11-18 12:06:16.702814386 +0100
+++ sql/schema-1.3.0.sql	2025-11-18 11:55:38.000000000 +0100
@@ -2,7 +2,7 @@
 -- PostgreSQL database dump
 --
 
-\restrict 8JTndBeGLLqHFHn8lhgfxPoYvgYfaWaapR62TSK7Prkm2kc7ZzD3YCCv7yYRth1
+\restrict iAvkiXKKNgmxB9XeO6PmCoy8hyuccZnduFdLS61sAPZEzsodGzoHNyCmMChkfmB
 
 -- Dumped from database version 13.22 (Debian 13.22-1.pgdg13+1)
 -- Dumped by pg_dump version 13.22 (Debian 13.22-1.pgdg13+1)
@@ -74,7 +74,8 @@
 CREATE TABLE public.tareas (
     id bigint NOT NULL,
     titulo character varying(255) NOT NULL,
-    usuario_id bigint NOT NULL
+    usuario_id bigint NOT NULL,
+    descripcion character varying(255)
 );
 
 
@@ -219,5 +220,5 @@
 -- PostgreSQL database dump complete
 --
 
-\unrestrict 8JTndBeGLLqHFHn8lhgfxPoYvgYfaWaapR62TSK7Prkm2kc7ZzD3YCCv7yYRth1
+\unrestrict iAvkiXKKNgmxB9XeO6PmCoy8hyuccZnduFdLS61sAPZEzsodGzoHNyCmMChkfmB
 
