# Proyecto de API de Firma Digital

[[EN]](https://github.com/dfandinodovalo/digital-signature-api/tree/develop/README.md) Versión del README.md en inglés.

## Descripción

Este proyecto implementa una **API REST** simple usando **Spring Boot 3.3.3** para la creación de usuarios, generación de claves de firma digital, firma de documentos y verificación de firmas. La API permite a los usuarios firmar documentos digitalmente y verificar la validez de esas firmas utilizando la biblioteca de **criptografía Java**. El proyecto utiliza una base de datos en memoria con **H2** para almacenar información y claves del usuario, lo que garantiza la persistencia de los datos durante el tiempo de ejecución.

## System Requirements

- Java 17 o superior
- Maven 3.6.3 o superior
- Spring Boot 3.3.3

## Instalación y Configuración

### Clonar el repositorio

```bash
git clone https://github.com/dfandinodovalo/digital-signature-api.git
cd digital-signature-api
```
### Construir el proyecto

El proyecto utiliza Maven para la gestión de dependencias. Para construir el proyecto, ejecuta:
```bash
mvn clean install
```

### Ejecutar la aplicación
```bash
mvn spring-boot:run
```

The application will run on `http://localhost:8080`.

### Ejecutar tests de la aplicación
```bash
mvn test
```

## Uso de la API

### Crear usuario
Crea un nuevo usuario.

- Endpoint: `POST` /api/user/create
- Request Body:
  ```json
  {
    "firstName": "string",
    "lastName": "string",
    "nif": "string"
  }
  ```
- Respuesta:
  ```json
  {
    "firstName": "string",
    "lastName": "string",
    "nif": "string"
  }
  ```

- Errores:
    - `409 Conflict` – `UserAlreadyExistsException`: El usuario ya existe.

### Generar claves del usuario
Genera un par de claves (pública y privada) para un usuario.

- Endpoint: `POST` /api/userkeys/generate-keys/{nif}
- Respuesta: cadena que indica que las claves se generaron con éxito.
- Errores:
    - `404 Not Found` – `UserNotFoundException`: El usuario no existe.
    - `409 Conflict` – `UserKeysAlreadyGeneratedException`: Las claves ya fueron generadas.
    - `500 Internal Server Error`: Error durante la generación de claves.



### Firmar un documento
Firma un documento con la clave privada del usuario.

- Endpoint: `POST` /api/sign
- Request Body:
  ```json
  {
    "documentBase64": "string",
    "nif": "string"
  }
  ```
- Response: espuesta: Firma digital codificada en Base64 del documento representada como una cadena.
- Errores:
    - `404 Not Found` – `UserNotFoundException`: El usuario no existe.
    - `404 Not Found` – `UserKeysNotFoundException`: TNo se encontraron las claves del usuario.
    - `500 Internal Server Error`: Error durante la firma del documento.



### Verificar la firma de un documento
Verifica si la firma es válida para el documento y usuario dados.

- Endpoint: POST /api/signature/verify
- Request Body:
  ```json
  {
    "documentBase64": "string",
    "signatureBase64": "string",
    "nif": "string"
  }
  ```
- Respuesta: valor booleano que indica si la firma es válida.
- Errores:
    - `404 Not Found` – `UserNotFoundException`: El usuario no existe.
    - `404 Not Found` – `UserKeysNotFoundException`: No se encontraron las claves del usuario.
    - `500 Internal Server Error`: Error durante la verificación de la firma.



## Colección de Postman

En el directorio [/postman](https://github.com/dfandinodovalo/digital-signature-api/tree/develop/postman), puedes encontrar el archivo [digitalSignatureApi-DavidFandino.postman_collection.json](https://github.com/dfandinodovalo/digital-signature-api/blob/develop/postman/digitalSignatureApi-DavidFandino.postman_collection.json). Esta colección de Postman contiene todas las solicitudes de la API preconfiguradas y listas para usar.


## Ejemplos con Curl

### Crear un usuario
  ```bash
curl -X POST http://localhost:8080/api/user/create \
    -H "Content-Type: application/json" \
    -d '{
        "firstName": "John",
        "lastName": "Doe",
        "nif": "12345678A"
    }'

  ```

### Generar claves del usuario
  ```bash
curl -X POST http://localhost:8080/api/userkeys/generate-keys/12345678A

  ```

### Firmar un documento
  ```bash
curl -X POST http://localhost:8080/api/sign \
    -H "Content-Type: application/json" \
    -d '{
        "documentBase64": "<base64_string>",
        "nif": "12345678A"
    }'

  ```

### Verificar una firma
  ```bash
curl -X POST http://localhost:8080/api/signature/verify \
    -H "Content-Type: application/json" \
    -d '{
        "documentBase64": "<base64_string>",
        "signatureBase64": "<signature_base64_string>",
        "nif": "12345678A"
    }'

  ```