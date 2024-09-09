# Digital Signature API Project

[[ES]](https://github.com/dfandinodovalo/digital-signature-api/tree/develop/language/README_ES.md) README.md version in Spanish.



## Description

This project implements a simple **REST API** using **Spring Boot 3.3.3** for user creation, digital signature key generation, document signing, and signature verification. The API allows users to digitally sign documents and verify the validity of those signatures using the **Java cryptography** library. The project uses an in-memory database with **H2** to store user information and keys, ensuring data persistence during runtime.

## System Requirements

- Java 17 or higher
- Maven 3.6.3 or higher
- Spring Boot 3.3.3

## Installation and Configuration

### Clone the repository

```bash
git clone https://github.com/dfandinodovalo/digital-signature-api.git
cd digital-signature-api
```
### Build the project

The project uses Maven for dependency management. To build the project, run:
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```

The application will run on `http://localhost:8080`.


### Run application tests
```bash
mvn test
```

## API Usage

### Create user

- Endpoint: `POST` /api/user/create
- Description: Creates a new user.
- Request Body:
  ```json
  {
    "firstName": "string",
    "lastName": "string",
    "nif": "string"
  }
  ```
- Response:
  ```json
  {
    "firstName": "string",
    "lastName": "string",
    "nif": "string"
  }
  ```

- Errors:
  - `409 Conflict` – `UserAlreadyExistsException`: The user already exists.
 
### Generate user keys
Generates a key pair (public and private) for a user.

- Endpoint: `POST` /api/userkeys/generate-keys/{nif}
- Response: string indicating that the keys were successfully generated.
- Errors:
  - `404 Not Found` – `UserNotFoundException`: The user does not exist.
  - `409 Conflict` – `UserKeysAlreadyGeneratedException`: The keys have already been generated.
  - `500 Internal Server Error`: Error during key generation.
 


### Sign a document
Signs a document with the user’s private key.

- Endpoint: `POST` /api/sign
- Request Body:
  ```json
  {
    "documentBase64": "string",
    "nif": "string"
  }
  ```
- Response: Base64-encoded digital signature of the document represented as a string.
- Errors:
  - `404 Not Found` – `UserNotFoundException`: The user does not exist.
  - `404 Not Found` – `UserKeysNotFoundException`: The user’s keys were not found.
  - `500 Internal Server Error`: Error during document signing.


 
### Verify document signature
Verifies if the signature is valid for the given document and user.

- Endpoint: POST /api/signature/verify
- Request Body:
  ```json
  {
    "documentBase64": "string",
    "signatureBase64": "string",
    "nif": "string"
  }
  ```
- Response: boolean indicating whether the signature is valid.
- Errors:
  - `404 Not Found` – `UserNotFoundException`: The user does not exist.
  - `404 Not Found` – `UserKeysNotFoundException`: The user’s keys were not found.
  - `500 Internal Server Error`: Error during signature verification.



## Postman Collection

In the directory [/postman](https://github.com/dfandinodovalo/digital-signature-api/tree/develop/postman), you can find the file [digitalSignatureApi-DavidFandino.postman_collection.json](https://github.com/dfandinodovalo/digital-signature-api/blob/develop/postman/digitalSignatureApi-DavidFandino.postman_collection.json). This Postman collection contains all the API requests pre-configured and ready to use.


## Examples with Curl

### Create a user
  ```bash
curl -X POST http://localhost:8080/api/user/create \
    -H "Content-Type: application/json" \
    -d '{
        "firstName": "John",
        "lastName": "Doe",
        "nif": "12345678A"
    }'

  ```

### Generate user keys
  ```bash
curl -X POST http://localhost:8080/api/userkeys/generate-keys/12345678A

  ```

### Sign a document
  ```bash
curl -X POST http://localhost:8080/api/sign \
    -H "Content-Type: application/json" \
    -d '{
        "documentBase64": "<base64_string>",
        "nif": "12345678A"
    }'

  ```

### Verify a signature
  ```bash
curl -X POST http://localhost:8080/api/signature/verify \
    -H "Content-Type: application/json" \
    -d '{
        "documentBase64": "<base64_string>",
        "signatureBase64": "<signature_base64_string>",
        "nif": "12345678A"
    }'

  ```
