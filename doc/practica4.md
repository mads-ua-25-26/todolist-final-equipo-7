# Memoria Técnica: Evolución a Arquitectura de Producción y GitFlow

## 1. Metodología de Trabajo: GitFlow
Para esta iteración, el equipo ha migrado de un desarrollo individual a un flujo de trabajo colaborativo basado en el estándar **GitFlow**. Esto nos ha permitido paralelizar tareas y asegurar la estabilidad del código.

* **Estructura de Ramas:** Se ha establecido una jerarquía donde `main` contiene únicamente código listo para producción y `develop` actúa como rama de integración continua.
* **Ciclo de Vida de Funcionalidades:** El desarrollo de nuevas características (como la gestión del perfil de usuario) se ha realizado en ramas aisladas (`feature/*`) que nacen de `develop`.
* **Gestión de Versiones:** Para la versión 1.3.0, se creó una rama específica `release/1.3.0`. Esto permitió "congelar" el código para realizar pruebas finales y preparar los metadatos (versión en `pom.xml`, fechas de publicación) sin bloquear el desarrollo continuo en `develop`.
* **Política de Integración:** Ningún cambio llega a las ramas principales directamente; todo se gestiona mediante *Pull Requests* (PR) que requieren la revisión y validación de código por parte de otro miembro del equipo antes del *merge*.

## 2. Evolución del Código y Funcionalidades (v1.3.0)
En esta versión se han introducido mejoras funcionales y correcciones respecto a la iteración anterior:

1.  **Modelo de Datos (Tareas):** Se ha extendido la entidad `Tarea` para incluir un campo de **descripción**, permitiendo un mayor nivel de detalle en los elementos de la lista.
2.  **Gestión de Usuarios:** Implementación de una nueva vista de **Perfil de Usuario**, donde el usuario autenticado puede consultar y editar sus datos personales.
3.  **Contenido Estático:** Corrección y actualización de la página `about.html` (Acerca de) para incluir la autoría completa del equipo, subsanando una omisión detectada en la versión previa.

## 3. Contenerización Flexible
Se ha refactorizado el `Dockerfile` y la configuración de arranque para desacoplar la aplicación de la infraestructura subyacente. Ahora, la imagen es agnóstica al entorno y permite inyectar configuraciones clave en tiempo de ejecución mediante variables de entorno, tales como:
* Selección de perfiles de Spring (`SPRING_PROFILES_ACTIVE`).
* Credenciales y URL de conexión a la base de datos externa.

## 4. Estrategia de Base de Datos en Producción

Para garantizar la integridad de los datos en un entorno real, se ha diseñado una estrategia de separación de entornos mediante perfiles:

### Perfil `postgres-prod`
Se ha creado un perfil específico para el despliegue final. Su característica crítica es la propiedad:
`spring.jpa.hibernate.ddl-auto=validate`

A diferencia del entorno de desarrollo (que puede crear o actualizar tablas automáticamente), este modo solo **valida** que el esquema de la base de datos existente coincida con las entidades de Java. Si hay discrepancias, la aplicación aborta el arranque, protegiendo así los datos existentes de modificaciones accidentales por parte del motor de persistencia.

### Control de Versiones de Esquema
Se han generado los scripts SQL necesarios para controlar la evolución de la BD:
* `sql/schema-1.2.0.sql`: Estado de la base de datos antes de los cambios.
* `sql/schema-1.3.0.sql`: Estado objetivo con las nuevas tablas y columnas.
* `sql/schema-1.2.0-1.3.0.sql`: **Script diferencial (migración)**. Contiene las sentencias `ALTER TABLE` necesarias para transformar la BD de la versión 1.2 a la 1.3 sin pérdida de datos.

## 5. Arquitectura del Despliegue en Producción

El despliegue se ha orquestado utilizando **Docker** para simular un entorno de servidor real, compuesto por dos servicios interconectados.

### A. Topología de Red
Se ha creado una red virtual interna (`network-equipo`) que permite la comunicación segura entre contenedores mediante resolución de nombres DNS (usando el alias `postgres` para la base de datos). Esto aísla el tráfico de base de datos, haciéndolo accesible solo para la aplicación y no desde el exterior (salvo mapeo explícito de puertos para mantenimiento).

### B. Persistencia de Datos (Servicio de BD)
El contenedor de base de datos (`db-equipo`) se ha configurado para ser persistente y accesible para tareas administrativas.
* **Volúmenes:** Se utiliza un *bind-mount* para conectar un directorio del host con `/mi-host` dentro del contenedor. Esto facilita operaciones de mantenimiento críticas como la inyección de scripts SQL de migración y la extracción de *backups* sin necesidad de detener el servicio.

### C. Despliegue de la Aplicación (Servicio Web)
La aplicación se despliega desde una imagen inmutable alojada en un registro público.
* **Imagen:** `yelesew/mads-todolist-equipo07`
* **Ejecución:** El contenedor se lanza conectado a la red `network-equipo` y configurado con el perfil `postgres-prod`, apuntando al host `postgres` (el contenedor de BD). Se expone el puerto 8080 para el acceso web.

### D. Protocolo de Actualización y Migración
Para validar el paso a producción de la versión 1.3.0, se ha ejecutado el siguiente protocolo de seguridad:

1.  **Simulación de entorno previo:** Arranque de una BD limpia y carga de un *backup* de la versión 1.2.0.
2.  **Migración manual:** Ejecución del script `schema-1.2.0-1.3.0.sql` sobre la base de datos en caliente para añadir el campo de descripción a las tareas existentes.
3.  **Despliegue de la App:** Arranque del contenedor de la aplicación (v1.3.0). Gracias a la validación de Hibernate, el arranque exitoso confirma que la migración de la base de datos fue correcta y compatible con el código.
4.  **Verificación Funcional:** Se comprobó manualmente que los datos antiguos (usuarios y tareas previas) persistían correctamente y que las nuevas funcionalidades (descripciones y perfil) estaban operativas.
5.  **Backup Final:** Generación de una nueva copia de seguridad (`backup-[FECHA].sql`) post-despliegue para consolidar el estado del sistema.

---
**Enlace a la imagen en Docker Hub:**
https://hub.docker.com/r/yelesew/mads-todolist-equipo07

---

## 6. Anexos: Detalles SQL

A continuación se detallan los cambios específicos a nivel de base de datos para cumplir con los requisitos de esquemas y migración.

### 6.1. Script de Migración (v1.2.0 a v1.3.0)
Sentencia SQL utilizada para actualizar la base de datos de producción sin pérdida de datos.
**Fichero:** `sql/schema-1.2.0-1.3.0.sql`

```sql
ALTER TABLE tareas ADD COLUMN descripcion VARCHAR(255);
```

## 7. Problemas que hemos tenido

Durante la realización de la práctica, nos dimos cuenta tarde de dos grandes fallos haciendo la rama de release-1.3.0:
1. No se modificó la base de datos en ninguna de las nuevas funcionalidades añadidas
2. No se hizo un pull de los cambios realizados por los otros compañeros al crear la rama release-1.3.0

Esto ha hecho que tuviéramos que modificar ambos fallos en esa rama para que todo estuviera correcto y en condiciones.

Otra cosa a comentar es que en la imagen de Docker Hub  no se visualiza el tag de la snapshot a causa de los pequeños problemas que hemos tenido durante el desarrollo.
A falta de comunicación y una repartición del trabajo de forma muy individual, ha habido confusiones con los dockers de la práctica.