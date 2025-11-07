# Práctica 3: Documentación Técnica

## Historia de Usuario 009: Gestionar Pertenencia al Equipo

Esta historia permite a los usuarios crear nuevos equipos y gestionar su participación en los mismos. Se han implementado tres endpoints principales para estas operaciones.

### Endpoints Implementados

#### GET `/equipos/nuevo`
**Método del Controller:** `EquiposController.formularioNuevoEquipo()`

Este endpoint renderiza el formulario para crear un nuevo equipo. El método verifica que el usuario esté autenticado mediante `managerUserSession.usuarioLogeado()` y devuelve la vista `formNuevoEquipo.html`.

```java
@GetMapping("/equipos/nuevo")
public String formularioNuevoEquipo(Model model, HttpSession session) {
    Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
    if (idUsuarioLogeado == null) {
        return "redirect:/login";
    }
    model.addAttribute("equipo", new EquipoData());
    return "formNuevoEquipo";
}
```

La plantilla Thymeleaf utiliza `th:object` para vincular el formulario con el objeto `EquipoData` y envía los datos mediante POST al mismo endpoint.

#### POST `/equipos/nuevo`
**Método del Controller:** `EquiposController.crearEquipo()`
**Método del Service:** `EquipoService.crearEquipo(String nombre)`

Este endpoint procesa la creación del equipo. El servicio se encarga de instanciar una nueva entidad `Equipo`, persistirla mediante `equipoRepository.save()` y devolver el DTO correspondiente usando ModelMapper.

**Test implementado:** `EquipoWebTest.postNuevoEquipoDevuelveRedirectYAñadeEquipo()` verifica que el equipo se crea correctamente y aparece en el listado posterior.

#### GET `/equipos/{idEquipo}/usuarios/{idUsuario}/agregar`
**Método del Controller:** `EquiposController.agregarUsuarioAEquipo()`
**Método del Service:** `EquipoService.añadirUsuarioAEquipo(Long idEquipo, Long idUsuario)`

Permite a un usuario unirse a un equipo existente. El método de servicio utiliza el método `addUsuario()` de la entidad `Equipo`, que actualiza ambos lados de la relación muchos-a-muchos:

```java
public void addUsuario(Usuario usuario) {
    this.getUsuarios().add(usuario);
    usuario.getEquipos().add(this);
}
```

Esta implementación es crucial porque JPA/Hibernate no actualiza automáticamente ambos lados de las relaciones bidireccionales.

#### GET `/equipos/{idEquipo}/usuarios/{idUsuario}/eliminar`
**Método del Controller:** `EquiposController.eliminarUsuarioDeEquipo()`
**Método del Service:** `EquipoService.eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario)`

Permite a un usuario abandonar un equipo. Similar al método de añadir usuario, utiliza `removeUsuario()` en la entidad `Equipo` para mantener la consistencia de la relación:

```java
public void removeUsuario(Usuario usuario) {
    this.getUsuarios().remove(usuario);
    usuario.getEquipos().remove(this);
}
```

**Test implementado:** `EquipoWebTest.eliminarUsuarioDeEquipoDevuelveRedirect()` comprueba que tras eliminar un usuario, este ya no aparece en el listado de miembros del equipo.

---

## Historia de Usuario 010: Gestión de Equipos (Administrador)

Esta funcionalidad está restringida exclusivamente a usuarios administradores y permite modificar y eliminar equipos.

### Control de Acceso Administrativo

En el controlador se implementó una verificación adicional para asegurar que solo los administradores pueden acceder a estas funcionalidades:

```java
if(!usuarioService.findById(idUsuarioLogeado).getAdmin()) {
    return "redirect:/equipos";
}
```

### Endpoints Implementados

#### GET `/equipos/{idEquipo}/editar`
**Método del Controller:** `EquiposController.formularioEditarEquipo()`

Muestra el formulario de edición del nombre del equipo. Solo es accesible por administradores.

**Plantilla:** `formEditarEquipo.html` utiliza `th:field="*{nombre}"` para el binding bidireccional con el objeto equipo.

#### POST `/equipos/{idEquipo}/editar`
**Método del Controller:** `EquiposController.editarEquipo()`
**Método del Service:** `EquipoService.editarNombreEquipo(Long idEquipo, String nombre)`

Procesa la actualización del nombre del equipo. La implementación del servicio es particularmente interesante:

```java
@Transactional
public void editarNombreEquipo(Long idEquipo, String nombre) {
    Equipo equipo = equipoRepository.findById(idEquipo)
            .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));
    equipo.setNombre(nombre);
    equipoRepository.saveAndFlush(equipo);
}
```

Se utiliza `saveAndFlush()` en lugar de `save()` para asegurar que los cambios se persisten inmediatamente en la base de datos, forzando la ejecución del UPDATE SQL antes de finalizar el método.

**Test implementado:** `EquipoWebTest.editarEquipoUsuarioAdminCambiaElNombre()` verifica que el nombre se actualiza correctamente en la base de datos.

### Aspecto Importante: Manejo del Campo Admin

Durante la implementación se identificó un problema con el campo `admin` de los usuarios. El ModelMapper no mapeaba correctamente este campo debido a que la entidad `Usuario` no tiene un setter convencional, sino métodos específicos `setAdmin()` y `disableAdmin()`. Se solucionó modificando el método `registrar()` en `UsuarioService`:

```java
@Transactional
public UsuarioData registrar(UsuarioData usuario) {
    // ... validaciones existentes
    Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
    
    if (usuario.getAdmin() != null && usuario.getAdmin()) {
        usuarioNuevo.setAdmin();
    } else {
        usuarioNuevo.disableAdmin();
    }
    
    usuarioNuevo = usuarioRepository.save(usuarioNuevo);
    return modelMapper.map(usuarioNuevo, UsuarioData.class);
}
```

### Visibilidad Condicional en las Vistas

En la plantilla `listadoEquipos.html` se implementó visibilidad condicional para el botón de edición usando Thymeleaf:

```html
<a class="btn btn-warning btn-xs"
   th:if="${usuario.admin}"
   th:href="@{/equipos/{idEquipo}/editar(idEquipo=${equipo.id})}">Editar</a>
```

Este enfoque proporciona una capa adicional de seguridad visual, aunque la verificación real se realiza en el controlador.

---

## Conclusiones

La implementación de estas historias de usuario ha permitido completar el ciclo de gestión de equipos en la aplicación. Se ha seguido correctamente la metodología TDD para la capa de servicio y repositorio, asegurando una cobertura de tests adecuada. Las funcionalidades administrativas añaden un nivel de control necesario para la gestión organizativa de la aplicación, manteniendo la separación de responsabilidades entre usuarios regulares y administradores.