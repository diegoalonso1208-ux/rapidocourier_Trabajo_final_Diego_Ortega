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
**Costos base por ruta:**

| Ruta | Costo Base |
|---|---|
| Lima → Lima | S/ 0.00 |
| Lima ↔ Arequipa | S/ 25.00 |
| Lima ↔ Cusco | S/ 35.00 |
| Arequipa ↔ Cusco | S/ 20.00 |

- `costoPorKg` = S/ 8.00
- `porcentajeSeguro` = 1% del valor declarado

**Ejemplo:** 1.5 kg, valor S/200, Lima→Arequipa:

---

## Estados y Transiciones del Paquete (RF-04)
| Estado Actual | Transiciones Válidas |
|---|---|
| REGISTRADO | EN_ALMACEN |
| EN_ALMACEN | EN_TRANSITO |
| EN_TRANSITO | EN_REPARTO |
| EN_REPARTO | ENTREGADO, NO_ENTREGADO |
| NO_ENTREGADO | EN_TRANSITO |
| ENTREGADO | (ninguno — estado final) |

---

## Comunicación Inter-Servicio

| Llamada | Tipo | Justificación |
|---|---|---|
| servicio-paquetes → servicio-clientes | Sincrónica (Feign) | Necesita validar cliente antes de registrar paquete |
| servicio-clientes → RENIEC | Sincrónica (Feign) | El nombre completo se necesita en tiempo real |
| Gateway → todos | Sincrónica (HTTP) | Enrutamiento stateless via Eureka |

**Datos replicados:** El nombre del remitente y destinatario se almacena en el
paquete para evitar llamadas constantes a servicio-clientes.

**Decisión difícil:** Se optó por comunicación sincrónica en vez de eventos
porque el registro de paquetes requiere confirmación inmediata de que el cliente
existe. Un enfoque asíncrono generaría paquetes en estado inconsistente.

---

## Instrucciones para Levantar el Sistema

### Pre-requisitos
- Java 17+, Maven, Docker Desktop

### 1. Levantar infraestructura Docker
```bash
docker-compose up -d
```

### 2. Configurar Vault
```bash
docker exec -it vault sh -c "VAULT_ADDR='http://127.0.0.1:8200' VAULT_TOKEN='myroot' vault kv put secret/servicio-auth jwt.secret=clave-super-secreta-rapidocourier-2024-hmac512-segura-para-produccion-minimo-64-caracteres"
```

### 3. Levantar servicios (cada uno en CMD como administrador)

```bash
# CMD 1
cd eureka-server && mvn spring-boot:run

# CMD 2
cd config-server && mvn spring-boot:run

# CMD 3
cd servicio-auth && mvn spring-boot:run

# CMD 4
cd servicio-clientes && mvn spring-boot:run

# CMD 5
cd servicio-paquetes && mvn spring-boot:run

# CMD 6
cd servicio-tarifas && mvn spring-boot:run

# CMD 7
cd api-gateway && mvn spring-boot:run
```

### 4. Verificar
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080

---

## Dashboard Eureka — Servicios en UP

> *(Adjuntar captura de pantalla del dashboard con todos los servicios UP)*

---

## Demostración Config Server Refresh

```bash
curl -X POST http://localhost:8888/actuator/refresh
```

---

## Estrategia de Seguridad JWT

La validación JWT se realiza en cada microservicio individualmente.
El API Gateway agrega headers de trazabilidad pero la autorización
se delega a cada servicio según su contexto de negocio.

**Roles:**
- **ADMIN**: puede eliminar registros, crear y actualizar paquetes
- **OPERADOR**: puede crear y actualizar paquetes
- **CLIENTE**: solo puede consultar sus propios paquetes

## Demostración Config Server Refresh

Cambiar una propiedad en el repositorio de configuración y ejecutar:

```bash
curl -X POST http://localhost:8888/actuator/refresh
```

Esto recarga las propiedades sin reiniciar el servicio. Con `@RefreshScope`
en `ClienteService`, el token de RENIEC se actualiza en caliente.