# RESPUESTAS CONCEPTUALES — RapidoCourier S.A.C.

## Pregunta 1
**¿Qué es un microservicio y en qué se diferencia de un monolito?**

Un microservicio es un componente de software pequeño, independiente y desplegable
de forma autónoma, que se enfoca en una única responsabilidad de negocio. Se comunica
con otros servicios a través de APIs bien definidas (REST o mensajería asíncrona).

Un monolito, en cambio, es una aplicación donde todas las funcionalidades están
integradas en un único proceso desplegable. Cualquier cambio requiere recompilar
y redesplegar toda la aplicación.
# RapidoCourier S.A.C. — Sistema de Microservicios

## Descripción
Sistema backend distribuido para gestión de envíos de paquetes entre sucursales
de Lima, Arequipa y Cusco. Construido con Spring Boot 3.2.5 y Spring Cloud 2023.0.1.

---

## Mapa de Microservicios

| Servicio | Puerto | BD | Bounded Context | RF implementados |
|---|---|---|---|---|
| eureka-server | 8761 | — | Registro de servicios | — |
| config-server | 8888 | — | Configuración centralizada | — |
| api-gateway | 8080 | — | Punto de entrada único | RNF-08 |
| servicio-auth | 8081 | PostgreSQL | Autenticación y usuarios | RF-08 |
| servicio-clientes | 8082 | PostgreSQL | Gestión de clientes | RF-01 |
| servicio-paquetes | 8083 | PostgreSQL | Paquetes, estados, historial | RF-02,03,04,05,06,07,09 |
| servicio-tarifas | 8084 | MongoDB | Cálculo de tarifas | RF-03 |

---

## Modelo de Datos por Servicio

### servicio-auth
- **usuarios**: id (UUID), nombre, email, password, role_id, createdAt, updatedAt
- **roles**: id (UUID), name (ADMIN/OPERADOR/CLIENTE)

### servicio-clientes
- **clientes**: id (UUID), dni, nombreCompleto, email, telefono, createdAt, updatedAt

### servicio-paquetes
- **paquetes**: id (UUID), codigoRastreo, descripcion, pesoKg, valorDeclarado,
  tarifa, sucursalOrigen, sucursalDestino, estado, remitenteId, destinatarioId,
  remitenteNombre, destinatarioNombre, createdAt, updatedAt
- **historial_estados**: id (UUID), paquete_id, estado, fechaCambio,
  usuarioResponsable, observacion
- **categorias**: id (UUID), nombre, descripcion
- **paquete_categorias**: paquete_id, categoria_id (tabla intermedia ManyToMany)

### servicio-tarifas (MongoDB)
- **tarifas_config**: id, origen, destino, costoPorKg, costoBase,
  porcentajeSeguro, activo

---

## Justificación de Base de Datos por Servicio

| Servicio | BD | Justificación |
|---|---|---|
| servicio-auth | PostgreSQL | Datos relacionales con integridad referencial entre usuarios y roles |
| servicio-clientes | PostgreSQL | Esquema fijo con restricción unique en DNI y email |
| servicio-paquetes | PostgreSQL | Relaciones complejas: ManyToMany con categorías, OneToMany con historial |
| servicio-tarifas | MongoDB | Configuraciones flexibles que cambian sin alterar esquema rígido |

---

## Arquitectura Interna

Todos los servicios usan arquitectura en capas tradicional:
- **Controller** → recibe HTTP, valida con @Valid, retorna ResponseEntity
- **Service** → lógica de negocio, transacciones
- **Repository** → acceso a datos con Spring Data
- **Entity/Document** → modelo de datos
- **DTO** → separación entre API pública y persistencia

---

## Regla de Cálculo de Tarifa (RF-03)
**Diferencias clave:**
- **Despliegue:** Los microservicios se despliegan independientemente; el monolito
  se despliega completo.
- **Escalabilidad:** En microservicios se escala solo el servicio que lo necesita;
  en monolito se escala todo.
- **Tecnología:** Cada microservicio puede usar su propia tecnología (políglota);
  el monolito usa una sola.
- **Fallo:** Un fallo en un microservicio no derriba el sistema completo; en un
  monolito sí.

En RapidoCourier aplicamos microservicios desde el primer día porque la empresa
necesita escalar independientemente el servicio de paquetes (alta demanda) sin
afectar el servicio de autenticación.

---

## Pregunta 2
**¿Qué es el Service Discovery y para qué sirve Eureka en este proyecto?**

Service Discovery es el mecanismo que permite a los microservicios encontrarse
entre sí dinámicamente, sin necesidad de conocer IPs o puertos fijos de antemano.

En RapidoCourier, **Eureka Server** actúa como un registro central donde cada
microservicio se registra al iniciar con su nombre lógico (ej: `servicio-clientes`)
y su ubicación de red. Cuando `servicio-paquetes` necesita comunicarse con
`servicio-clientes`, consulta a Eureka por el nombre lógico y obtiene la dirección
actual, sin hardcodear ninguna URL.

**Beneficios en el proyecto:**
- El API Gateway enruta usando `lb://servicio-auth` en vez de `localhost:8081`
- Si un servicio cambia de puerto, los demás lo descubren automáticamente
- Permite balanceo de carga entre múltiples instancias del mismo servicio

---

## Pregunta 3
**¿Qué es un Circuit Breaker y por qué lo usamos en RapidoCourier?**

Un Circuit Breaker (disyuntor) es un patrón de resiliencia que protege al sistema
de fallos en cascada. Funciona como un interruptor eléctrico: cuando detecta que
un servicio externo está fallando repetidamente, "abre el circuito" y deja de
llamarlo temporalmente, retornando una respuesta degradada (fallback).

**Estados:**
- **Cerrado (normal):** Las llamadas pasan normalmente
- **Abierto (fallo):** Las llamadas se cortan y se ejecuta el fallback
- **Semi-abierto (recuperación):** Se permiten algunas llamadas de prueba

**En RapidoCourier lo usamos en:**
- `servicio-clientes` → llamadas a RENIEC: si RENIEC falla, retorna error 502
  controlado en vez de que el sistema colapse
- `servicio-paquetes` → llamadas a `servicio-clientes`: si clientes está caído,
  retorna respuesta degradada funcional

Configuración usada:
- `failureRateThreshold: 50` → abre si el 50% de llamadas fallan
- `waitDurationInOpenState: 10s` → espera 10 segundos antes de intentar recuperarse
- `slidingWindowSize: 5` → evalúa las últimas 5 llamadas

---

## Pregunta 4
**¿Qué es el API Gateway y qué ventajas aporta al sistema?**

El API Gateway es el único punto de entrada al sistema para los clientes externos.
Actúa como un proxy inverso que enruta las peticiones al microservicio correspondiente.

**En RapidoCourier, el API Gateway:**
- Recibe todas las peticiones en el puerto 8080
- Enruta a cada servicio usando balanceo de carga via Eureka (`lb://`)
- Aplica un filtro global que agrega headers de trazabilidad (`X-Request-Id`,
  `X-Gateway-Timestamp`) a cada request
- Es el punto donde se podría centralizar la autenticación JWT

**Ventajas:**
- Los microservicios no están expuestos directamente al exterior
- Un solo punto para aplicar seguridad, logging y monitoreo
- Simplifica el cliente: solo necesita conocer una URL
- Permite cambiar la arquitectura interna sin afectar a los clientes

---

## Pregunta 5
**¿Qué es la persistencia políglota y por qué la aplicamos en este proyecto?**

La persistencia políglota es el uso de diferentes tecnologías de base de datos
según las necesidades específicas de cada microservicio, en vez de usar una sola
base de datos para todo el sistema.

**En RapidoCourier:**

| Servicio | Base de Datos | Justificación |
|---|---|---|
| servicio-auth | PostgreSQL | Datos relacionales: usuarios y roles con relaciones FK |
| servicio-clientes | PostgreSQL | Datos estructurados: clientes con esquema fijo |
| servicio-paquetes | PostgreSQL | Relaciones complejas: paquetes, historial, categorías |
| servicio-tarifas | MongoDB | Documentos flexibles: reglas de tarifa que cambian frecuentemente |

**¿Por qué MongoDB para tarifas?**
Las reglas de cálculo de tarifa son configuraciones que pueden cambiar sin alterar
un esquema relacional rígido. MongoDB permite agregar nuevos campos (descuentos,
temporadas, tipos de cliente) sin migraciones costosas. Es ideal para datos de
configuración flexible.

**Principio clave:** Cada microservicio es dueño exclusivo de su base de datos.
Ningún servicio accede directamente a la base de datos de otro; la comunicación
se hace únicamente via APIs REST.