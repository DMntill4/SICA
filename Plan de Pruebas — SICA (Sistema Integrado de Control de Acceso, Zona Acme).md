# Plan de Pruebas — SICA (Sistema Integrado de Control de Acceso, Zona Acme)

Documento de QA para validar funcionalmente el sistema contra el enunciado. Organizado por módulo. Cada caso incluye: ID, Descripción, Precondición, Pasos, Resultado Esperado, Tipo.

Leyenda de Tipo: **POS** = positivo/happy path · **NEG** = negativo/error · **EDGE** = caso límite · **SEC** = seguridad · **AUD** = auditoría · **CONC** = concurrencia · **NF** = no funcional

---

## 1. Autenticación y Sesión

| ID      | Descripción                                         | Precondición                     | Pasos                                             | Resultado Esperado                                           | Tipo |
| ------- | --------------------------------------------------- | -------------------------------- | ------------------------------------------------- | ------------------------------------------------------------ | ---- |
| AUTH-01 | Login exitoso con credenciales válidas              | Usuario activo existe            | Ingresar usuario/clave correctos                  | Acceso concedido, sesión creada, se ve el menú acorde a su rol | POS  |
| AUTH-02 | Login fallido - clave incorrecta                    | Usuario existe                   | Ingresar clave errónea 1 vez                      | Acceso denegado, mensaje genérico (no revela si el usuario existe) | NEG  |
| AUTH-03 | Login fallido - usuario inexistente                 | —                                | Ingresar usuario que no existe                    | Acceso denegado, mismo mensaje genérico que AUTH-02          | NEG  |
| AUTH-04 | Login fallido registrado en bitácora                | Usuario existe                   | Fallar login                                      | Se inserta registro en `bitacora_auditoria` con acción "login fallido" | AUD  |
| AUTH-05 | Login exitoso registrado en bitácora                | Usuario existe                   | Login correcto                                    | Se inserta registro con acción "login exitoso"               | AUD  |
| AUTH-06 | Usuario inactivo/deshabilitado no puede loguear     | Usuario marcado inactivo         | Intentar login                                    | Acceso denegado con mensaje específico de cuenta inactiva    | NEG  |
| AUTH-07 | Bloqueo por intentos fallidos repetidos (si aplica) | N intentos fallidos configurados | Fallar login N veces seguidas                     | Cuenta se bloquea temporalmente / se notifica                | EDGE |
| AUTH-08 | Cierre de sesión                                    | Sesión activa                    | Logout                                            | Sesión termina, no se puede navegar a rutas protegidas sin volver a loguear | POS  |
| AUTH-09 | Acceso a ruta protegida sin sesión                  | Sin login                        | Navegar directamente a una URL/endpoint protegido | Redirige a login / rechaza con 401-403                       | SEC  |
| AUTH-10 | Contraseña almacenada con hash                      | —                                | Inspeccionar tabla usuarios en BD                 | La contraseña NO está en texto plano (hash bcrypt/similar)   | SEC  |
| AUTH-11 | Inyección SQL en campos de login                    | —                                | Ingresar `' OR '1'='1` en usuario/clave           | Sistema rechaza, no hay bypass ni error de BD expuesto       | SEC  |
| AUTH-12 | Campos vacíos en login                              | —                                | Enviar formulario sin usuario o sin clave         | Validación impide el envío / mensaje de campo requerido      | NEG  |

---

## 2. RBAC — Roles, Permisos y Autorización

| ID      | Descripción                                                  | Precondición                                              | Pasos                                                        | Resultado Esperado                                           | Tipo |
| ------- | ------------------------------------------------------------ | --------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ---- |
| RBAC-01 | Crear un nuevo rol                                           | Usuario con permiso `gestionar_roles`                     | Crear rol "Recepcionista"                                    | Rol creado y persistido en BD                                | POS  |
| RBAC-02 | Asignar permisos a un rol                                    | Rol existente                                             | Asociar permisos ej. `registrar_visita`, `generar_reporte`   | Permisos quedan asociados al rol en tabla intermedia         | POS  |
| RBAC-03 | Permiso no definido en código, sino en BD                    | —                                                         | Revisar el código fuente                                     | No hay checks de rol hardcodeados tipo `if role == "ADMIN"`; se consulta permisos desde BD | POS  |
| RBAC-04 | Usuario con rol sin el permiso requerido intenta ejecutar acción | Usuario con rol limitado (ej. Guarda sin `crear_usuario`) | Intentar crear un usuario                                    | Operación denegada con mensaje claro de falta de permiso     | NEG  |
| RBAC-05 | Usuario con el permiso exacto sí puede ejecutar la acción    | Usuario con `crear_usuario`                               | Crear un usuario                                             | Operación permitida                                          | POS  |
| RBAC-06 | Revocar un permiso de un rol y verificar efecto inmediato    | Rol con permiso asignado                                  | Quitar el permiso, reintentar la acción (misma sesión o nueva) | La acción ahora es denegada                                  | POS  |
| RBAC-07 | Eliminar un rol que tiene usuarios asignados                 | Rol en uso                                                | Intentar eliminar el rol                                     | Se bloquea la eliminación o se maneja de forma explícita (reasignación/advertencia) | EDGE |
| RBAC-08 | Un mismo permiso protege múltiples endpoints/acciones coherentemente | Dos funcionalidades que comparten permiso                 | Probar ambas con y sin el permiso                            | Comportamiento consistente en ambas                          | POS  |
| RBAC-09 | Intentar autorizar una acción manipulando el rol en el cliente (frontend) | Sesión de usuario con rol limitado                        | Modificar rol vía DevTools/petición directa al backend       | El backend re-valida server-side y rechaza; el frontend no es la única barrera | SEC  |
| RBAC-10 | Cada acción crítica del enunciado tiene su propio permiso individual | —                                                         | Revisar catálogo de permisos                                 | Existen permisos separados: `crear_usuario`, `registrar_visita`, `generar_reporte`, `bloquear_persona`, etc. (no un solo permiso genérico) | POS  |
| RBAC-11 | Usuario sin ningún rol asignado                              | Usuario recién creado sin rol                             | Intentar loguear/operar                                      | No tiene acceso a ninguna acción protegida                   | EDGE |

---

## 3. Gestión de Usuarios, Personas y Empresas

| ID     | Descripción                                               | Precondición                                  | Pasos                                                | Resultado Esperado                                           | Tipo     |
| ------ | --------------------------------------------------------- | --------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------ | -------- |
| USR-01 | Crear usuario con datos válidos                           | Permiso adecuado                              | Completar formulario y guardar                       | Usuario creado, aparece en listado                           | POS      |
| USR-02 | Crear usuario con email/documento duplicado               | Usuario ya existe con ese identificador único | Intentar crear otro igual                            | Sistema rechaza por duplicado                                | NEG      |
| USR-03 | Editar datos de un usuario                                | Usuario existente                             | Modificar nombre/rol                                 | Cambios persistidos, bitácora registra la actualización      | POS      |
| USR-04 | Eliminar (o desactivar) un usuario                        | Usuario existente                             | Ejecutar eliminación                                 | Usuario ya no puede loguear; bitácora registra la eliminación | POS      |
| USR-05 | Crear una Empresa (inquilino de Zona Acme)                | Permiso adecuado                              | Registrar empresa con datos                          | Empresa creada y disponible para asociar Funcionarios/Personas | POS      |
| USR-06 | Registrar una Persona (trabajador) asociada a una empresa | Empresa existente                             | Crear persona con documento, foto URL, empresa       | Persona creada correctamente                                 | POS      |
| USR-07 | Registrar Persona con documento inválido/vacío            | —                                             | Enviar formulario sin documento                      | Validación rechaza el registro                               | NEG      |
| USR-08 | Buscar persona por número de documento                    | Persona registrada                            | Buscar por documento exacto                          | Se retorna la persona correcta                               | POS      |
| USR-09 | Buscar persona con documento inexistente                  | —                                             | Buscar documento que no existe                       | Mensaje de "no encontrado", sin error 500                    | NEG      |
| USR-10 | Campos con caracteres especiales / muy largos             | —                                             | Ingresar nombre con 300 caracteres, emojis, SQL/HTML | Sistema valida longitud y sanitiza input (sin XSS/SQLi)      | SEC/EDGE |

---

## 4. Control de Acceso — Flujo 1: Invitado Pre-registrado

| ID     | Descripción                                                  | Precondición                      | Pasos                                                | Resultado Esperado                                           | Tipo |
| ------ | ------------------------------------------------------------ | --------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------ | ---- |
| VIS-01 | Funcionario pre-registra un invitado                         | Funcionario logueado con permiso  | Ingresar datos del invitado, fecha/hora de visita    | Visita creada con estado "Aprobado"                          | POS  |
| VIS-02 | Guarda busca invitado pre-registrado por documento           | Visita en estado Aprobado         | Buscar documento en pantalla del guarda              | Se muestra info completa: datos, foto (URL), a quién visita, estado autorizado | POS  |
| VIS-03 | Guarda realiza check-in de invitado aprobado                 | Visita Aprobado                   | Ejecutar check-in                                    | Estado pasa a "Dentro", queda hora de ingreso registrada, bitácora actualizada | POS  |
| VIS-04 | Buscar invitado pre-registrado pero que llega en fecha distinta a la registrada | Visita programada para otro día   | Guarda busca documento                               | Sistema indica que la visita no corresponde a la fecha actual (regla de negocio a validar) | EDGE |
| VIS-05 | Invitado pre-registrado con foto URL inválida/rota           | Visita con URL de foto malformada | Guarda consulta el registro                          | La pantalla no rompe (maneja imagen faltante con placeholder), resto de datos visibles | EDGE |
| VIS-06 | Check-in duplicado del mismo invitado el mismo día           | Invitado ya hizo check-in         | Intentar check-in de nuevo sin haber hecho check-out | Sistema debe impedir o alertar de doble ingreso sin salida   | NEG  |
| VIS-07 | Check-out del invitado                                       | Invitado en estado "Dentro"       | Ejecutar check-out                                   | Estado pasa a "Finalizada"/"Fuera", hora de salida registrada | POS  |

---

## 5. Control de Acceso — Flujo 2: Invitado No Anunciado (Tiempo Real)

| ID        | Descripción                                                  | Precondición                               | Pasos                                                        | Resultado Esperado                                           | Tipo |
| --------- | ------------------------------------------------------------ | ------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ---- |
| WALKIN-01 | Guarda registra invitado sin cita previa                     | Guarda logueado                            | Ingresar datos básicos del invitado y a quién visita         | Visita creada con estado "Pendiente de Aprobación"           | POS  |
| WALKIN-02 | Notificación llega al Funcionario correspondiente            | Visita pendiente creada                    | Observar la interfaz del Funcionario (sin recargar manualmente) | La solicitud aparece en tiempo real (push/polling/websocket) sin refresh manual | CONC |
| WALKIN-03 | Funcionario aprueba la visita                                | Solicitud pendiente visible                | Click en "Aprobar"                                           | Estado cambia a "Aprobado"; pantalla del Guarda se actualiza sola en tiempo real | CONC |
| WALKIN-04 | Funcionario rechaza la visita                                | Solicitud pendiente visible                | Click en "Rechazar"                                          | Estado cambia a "Rechazado"; pantalla del Guarda refleja que no se permite el ingreso | CONC |
| WALKIN-05 | Guarda intenta dar ingreso antes de la aprobación            | Visita aún "Pendiente"                     | Intentar check-in manualmente                                | Sistema lo impide mientras el estado no sea "Aprobado"       | NEG  |
| WALKIN-06 | Dos guardas distintos consultan la misma solicitud pendiente simultáneamente | Solicitud pendiente                        | Ambos guardas abren la pantalla al mismo tiempo              | Ambos ven el mismo estado consistente; al aprobarse, ambos se actualizan | CONC |
| WALKIN-07 | Funcionario correspondiente no está disponible / no responde | Solicitud pendiente por tiempo prolongado  | Esperar / revisar comportamiento                             | Definir y validar comportamiento: ¿expira?, ¿escala a otro rol?, ¿queda pendiente indefinidamente? | EDGE |
| WALKIN-08 | Notificación dirigida al funcionario correcto (no a otros)   | Varias empresas con funcionarios distintos | Crear visita para Empresa A                                  | Solo el/los funcionarios de Empresa A ven la notificación, no los de Empresa B | SEC  |

---

## 6. Control de Acceso — Flujo 3: Trabajador con Carnet Olvidado

| ID        | Descripción                                            | Precondición                                              | Pasos                                                        | Resultado Esperado                                           | Tipo |
| --------- | ------------------------------------------------------ | --------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ---- |
| FORGET-01 | Guarda registra ingreso por olvido de carnet           | Trabajador identificable en sistema, sin documento físico | Buscar trabajador, marcar "Pendiente de Aprobación por Olvido" | Registro creado con ese estado específico (distinto al de invitado) | POS  |
| FORGET-02 | Funcionario aprueba el pase puntual                    | Solicitud por olvido pendiente                            | Aprobar                                                      | Se autoriza el ingreso solo para el día actual               | POS  |
| FORGET-03 | Verificar que la aprobación es puntual (no permanente) | Aprobación de olvido concedida                            | Consultar al día siguiente si sigue "aprobado"               | El pase no debe seguir vigente al día siguiente; requiere nuevo trámite | EDGE |
| FORGET-04 | Funcionario rechaza el pase por olvido                 | Solicitud pendiente                                       | Rechazar                                                     | Ingreso denegado, estado queda "Rechazado"                   | NEG  |
| FORGET-05 | Trabajador con múltiples olvidos en la semana          | Varios registros de olvido                                | Revisar histórico / reporte                                  | El sistema conserva todos los registros para trazabilidad/auditoría (no sobreescribe) | AUD  |

---

## 7. Control de Acceso — Flujo 4: Salida Olvidada (Regularización)

| ID     | Descripción                                                  | Precondición                                 | Pasos                                                | Resultado Esperado                                           | Tipo |
| ------ | ------------------------------------------------------------ | -------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------ | ---- |
| REG-01 | Persona con visita anterior en estado "Dentro" intenta nuevo ingreso | Existe visita previa abierta sin check-out   | Guarda registra nuevo ingreso de esa persona         | Sistema detecta la inconsistencia automáticamente            | POS  |
| REG-02 | Visita anterior se cierra automáticamente                    | Mismo escenario que REG-01                   | Ejecutar el nuevo ingreso                            | La visita previa cambia a estado "Cerrada por Sistema (Salida Olvidada)" | POS  |
| REG-03 | Se crea nueva visita para el ingreso actual                  | Mismo escenario                              | Ejecutar el nuevo ingreso                            | Se genera un nuevo registro de visita en estado "Dentro" para el ingreso actual | POS  |
| REG-04 | El ingreso NO se bloquea pese a la inconsistencia            | Mismo escenario                              | Ejecutar el nuevo ingreso                            | El acceso se concede con normalidad (no hay bloqueo)         | POS  |
| REG-05 | La regularización queda registrada en bitácora               | Mismo escenario                              | Revisar `bitacora_auditoria`                         | Existe un registro explícito del cierre automático por sistema | AUD  |
| REG-06 | Reporte/consulta puede diferenciar cierres normales vs. cierres por sistema | Hay visitas con ambos tipos de cierre        | Generar reporte de histórico de una persona          | Se distingue claramente el estado "Cerrada por Sistema" del cierre manual normal | POS  |
| REG-07 | Persona con dos o más salidas olvidadas consecutivas (edge extremo) | Historial con múltiples aperturas sin cierre | Repetir el escenario de olvido varias veces seguidas | Cada ingreso nuevo cierra correctamente solo la visita previa abierta, sin duplicar ni corromper otras | EDGE |

---

## 8. Gestión de Incidentes y Bloqueo de Personas

| ID     | Descripción                                                 | Precondición                              | Pasos                                                        | Resultado Esperado                                           | Tipo |
| ------ | ----------------------------------------------------------- | ----------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ---- |
| INC-01 | Registrar un incidente asociado a una persona               | Permiso `registrar_incidente` (o similar) | Completar formulario de incidente                            | Incidente guardado y visible en histórico de la persona      | POS  |
| INC-02 | Bloquear el acceso de una persona                           | Permiso `bloquear_persona`                | Marcar a la persona como bloqueada                           | Estado de la persona cambia a "Bloqueado"/"Restringido"      | POS  |
| INC-03 | Intento de ingreso de una persona bloqueada                 | Persona con estado bloqueado              | Guarda busca su documento e intenta check-in                 | Sistema impide el ingreso y muestra alerta clara del bloqueo en TODOS los puntos de entrada | POS  |
| INC-04 | Verificar que el bloqueo aplica de forma inmediata y global | Persona recién bloqueada                  | Intentar ingreso desde otra sesión de guarda inmediatamente después del bloqueo | El bloqueo ya es efectivo sin demora ni necesidad de reiniciar sesión | CONC |
| INC-05 | Desbloquear a una persona                                   | Persona bloqueada                         | Ejecutar desbloqueo con el permiso adecuado                  | Persona puede ingresar de nuevo con normalidad               | POS  |
| INC-06 | Bloqueo/desbloqueo queda registrado en bitácora             | Cualquiera de los pasos anteriores        | Revisar bitácora                                             | Existe registro de "cambio de estado de acceso" para esa persona | AUD  |
| INC-07 | Usuario sin permiso de bloqueo intenta bloquear a alguien   | Usuario con rol limitado                  | Intentar bloquear una persona                                | Operación denegada                                           | NEG  |

---

## 9. Reportes

| ID     | Descripción                                                 | Precondición                               | Pasos                                                     | Resultado Esperado                                           | Tipo |
| ------ | ----------------------------------------------------------- | ------------------------------------------ | --------------------------------------------------------- | ------------------------------------------------------------ | ---- |
| REP-01 | Generar reporte de aforo actual (quién está dentro)         | Personas con visitas activas               | Ejecutar reporte                                          | Lista coincide exactamente con las visitas en estado "Dentro" | POS  |
| REP-02 | Generar reporte histórico de accesos por persona            | Persona con múltiples visitas              | Filtrar por persona y rango de fechas                     | Se listan todas sus visitas dentro del rango, con estados correctos | POS  |
| REP-03 | Generar reporte histórico por empresa                       | Empresa con varios trabajadores/visitantes | Filtrar por empresa                                       | Se listan solo los registros de esa empresa                  | POS  |
| REP-04 | Reporte con rango de fechas sin resultados                  | Filtro fuera de cualquier dato existente   | Ejecutar reporte                                          | Se retorna lista vacía con mensaje claro, no error           | NEG  |
| REP-05 | Usuario sin permiso `generar_reporte` intenta generar uno   | Rol limitado                               | Intentar acceder al módulo de reportes                    | Operación denegada                                           | NEG  |
| REP-06 | Reporte de incidentes por persona/período                   | Incidentes registrados                     | Ejecutar reporte de incidentes                            | Datos coinciden con lo registrado en INC-01                  | POS  |
| REP-07 | Exportar reporte (si aplica: PDF/Excel/CSV)                 | Reporte generado                           | Exportar                                                  | Archivo se descarga correctamente con los mismos datos mostrados en pantalla | POS  |
| REP-08 | Uso de Stream API/lambdas no altera el resultado de negocio | Dataset grande simulado                    | Comparar resultado de reporte con cálculo manual/esperado | Resultados coinciden (validación funcional, no solo de código) | POS  |

---

## 10. Bitácora de Auditoría (Inmutabilidad y Cobertura)

| ID     | Descripción                                                  | Precondición                                                 | Pasos                                                        | Resultado Esperado                                           | Tipo |
| ------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ---- |
| AUD-01 | Cada login (éxito/fallo) genera registro                     | —                                                            | Loguear y fallar login                                       | Ambos casos generan entrada en bitácora                      | AUD  |
| AUD-02 | CRUD de Usuarios genera registros                            | Permiso adecuado                                             | Crear, editar, eliminar un usuario                           | 3 registros distintos en bitácora con acción correspondiente | AUD  |
| AUD-03 | CRUD de Personas genera registros                            | Permiso adecuado                                             | Crear, editar, eliminar una persona                          | Registros en bitácora por cada operación                     | AUD  |
| AUD-04 | CRUD de Empresas genera registros                            | Permiso adecuado                                             | Crear, editar, eliminar una empresa                          | Registros en bitácora por cada operación                     | AUD  |
| AUD-05 | Cambio de estado de acceso de una persona genera registro    | Persona existente                                            | Bloquear/desbloquear                                         | Registro en bitácora                                         | AUD  |
| AUD-06 | Registro de incidente genera entrada en bitácora             | —                                                            | Crear incidente                                              | Registro en bitácora                                         | AUD  |
| AUD-07 | Check-in y check-out generan registros separados             | Visita activa                                                | Hacer check-in y luego check-out                             | Dos entradas distintas en bitácora (una por evento)          | AUD  |
| AUD-08 | La bitácora es inmutable: no existe endpoint/UI para editar o borrar registros | Registros existentes                                         | Intentar editar o eliminar un registro de bitácora (UI y API directa) | Operación no disponible / rechazada en todos los niveles     | SEC  |
| AUD-09 | El registro de bitácora incluye quién, qué, cuándo           | Cualquier acción registrada                                  | Inspeccionar un registro                                     | Contiene usuario ejecutor, acción, entidad afectada y timestamp | AUD  |
| AUD-10 | Falla controlada de una operación no deja el sistema en estado inconsistente con la bitácora | Forzar un error a mitad de una operación (ej. BD caída simulada) | Ejecutar la operación                                        | Si la operación de negocio falla, no debe quedar un registro de bitácora "fantasma" indicando éxito | EDGE |

---

## 11. Concurrencia y Tiempo Real (transversal)

| ID      | Descripción                                                  | Precondición                | Pasos                                        | Resultado Esperado                                           | Tipo |
| ------- | ------------------------------------------------------------ | --------------------------- | -------------------------------------------- | ------------------------------------------------------------ | ---- |
| CONC-01 | Dos guardas intentan dar check-in a la misma persona al mismo tiempo | Persona sin visita activa   | Ejecutar ambos check-in casi simultáneamente | Solo se crea UNA visita "Dentro"; no hay duplicados por condición de carrera | CONC |
| CONC-02 | Aprobación de un funcionario mientras el guarda tiene la pantalla abierta | Solicitud pendiente         | Funcionario aprueba mientras guarda observa  | La pantalla del guarda se actualiza sin necesidad de refrescar manualmente | CONC |
| CONC-03 | Dos funcionarios distintos intentan aprobar/rechazar la misma solicitud a la vez | Solicitud pendiente única   | Ambos actúan casi simultáneamente            | Solo se aplica una decisión final consistente (gana la primera, la segunda se informa que ya fue resuelta) | CONC |
| CONC-04 | Bloqueo de persona mientras está intentando hacer check-in   | Persona a punto de ingresar | Bloquear justo antes/durante el check-in     | El sistema no permite el ingreso una vez aplicado el bloqueo | CONC |

---

## 12. Pruebas No Funcionales

| ID    | Descripción                                                  | Tipo   |
| ----- | ------------------------------------------------------------ | ------ |
| NF-01 | Tiempo de respuesta de búsqueda de persona por documento es aceptable (< 1-2 seg) bajo carga normal | NF     |
| NF-02 | La aplicación maneja correctamente errores de conexión a BD sin exponer stack traces al usuario final | NF/SEC |
| NF-03 | Mensajes de error son claros y en español, sin jerga técnica para roles no técnicos (Guarda) | NF     |
| NF-04 | La UI del Guarda es usable en flujo de alta velocidad (pocos clics para check-in de un pre-registrado) | NF     |
| NF-05 | El sistema no permite XSS almacenado en campos de texto libre (ej. nombre, motivo de incidente) | SEC    |
| NF-06 | Contraseñas y datos sensibles no se filtran en logs de aplicación ni en respuestas de error | SEC    |
| NF-07 | Rutas de API respetan verbos HTTP y códigos de estado correctos (200/201/400/401/403/404/409/500) | NF     |
| NF-08 | El sistema soporta al menos un volumen razonable de personas/visitas sin degradarse (prueba de carga básica) | NF     |

---

## 13. Integridad de Base de Datos y Scripts

| ID    | Descripción                                                  | Pasos                                        | Resultado Esperado                                           | Tipo     |
| ----- | ------------------------------------------------------------ | -------------------------------------------- | ------------------------------------------------------------ | -------- |
| DB-01 | `schema.sql` crea todas las tablas sin errores en una BD limpia | Ejecutar script desde cero                   | Todas las tablas, claves foráneas y restricciones se crean correctamente | POS      |
| DB-02 | `data.sql` puebla datos de ejemplo sin violar restricciones  | Ejecutar tras `schema.sql`                   | Inserciones exitosas, incluye al menos un usuario por cada rol para pruebas | POS      |
| DB-03 | Integridad referencial: no se puede borrar una Empresa con Personas asociadas (o se maneja explícitamente) | Intentar borrar empresa con dependientes     | Operación bloqueada o con estrategia definida (cascada/restricción) | EDGE     |
| DB-04 | Tabla `bitacora_auditoria` no tiene FK que permita borrar en cascada sus registros al borrar otras entidades | Borrar una entidad referenciada en bitácora  | Los registros de bitácora relacionados persisten (no se pierden) | AUD      |
| DB-05 | Campos únicos (documento de persona, email de usuario) tienen restricción UNIQUE real a nivel de BD, no solo en la app | Intentar inserción duplicada directo por SQL | La BD rechaza el insert                                      | SEC/EDGE |

---

## 14. Documentación y Entregables (checklist final)

| ID     | Descripción                                                  | Verificación                                                 |
| ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| DOC-01 | README incluye descripción del problema y la solución        | Revisar sección                                              |
| DOC-02 | README incluye el diagrama Entidad-Relación (imagen o Mermaid) | Revisar que el diagrama existe y coincide con el `schema.sql` real |
| DOC-03 | README explica dónde se aplicó cada principio SOLID con ejemplo concreto de clase/método | Revisar que no sean afirmaciones genéricas sin evidencia en el código |
| DOC-04 | README explica los patrones de diseño usados (mínimo 2), con justificación de por qué se eligieron | Revisar coherencia con el código real                        |
| DOC-05 | Instrucciones de instalación funcionan tal cual están escritas | Clonar el repo en limpio y seguir el README paso a paso      |
| DOC-06 | Guía de uso incluye credenciales de ejemplo para cada rol del sistema | Probar login con cada credencial listada                     |
| DOC-07 | El repositorio sigue Git Flow (ramas `main`/`develop`/`feature/*`) | Revisar historial de ramas                                   |
| DOC-08 | Los commits siguen Conventional Commits (`feat:`, `fix:`, `docs:`, etc.) | Revisar `git log`                                            |
| DOC-09 | El trainer figura como colaborador del repositorio privado   | Revisar configuración del repo en GitHub                     |
| DOC-10 | La estructura de paquetes refleja la Arquitectura Hexagonal + Vertical Slice mencionada | Revisar árbol de directorios del código fuente               |

---

## Cómo priorizar la ejecución si el tiempo es limitado

1. **Bloqueantes (probar primero):** AUTH-01/02, RBAC-04/05/09, VIS-01 a VIS-03, WALKIN-01 a WALKIN-05, REG-01 a REG-05, INC-02/03, AUD-01 a AUD-07.
2. **Importantes:** el resto de RBAC, reportes, incidentes, no funcionales de seguridad (SEC).
3. **Refinamiento:** casos EDGE y NF de rendimiento/usabilidad.

Si estos tres bloques pasan, cubres la lógica de negocio central (los 4 flujos de acceso), el RBAC configurable, y la bitácora — que son los tres pilares explícitamente exigidos por el enunciado.