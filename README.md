# BungeeAuth Plugin

BungeeAuth es un plugin de autenticación para BungeeCord que proporciona funcionalidades de registro, inicio de sesión y gestión de cuentas para servidores de Minecraft.

## Características

- Almacenamiento de datos de usuario en MongoDB
- Encriptacion de contraseñas utilizando BCrypt
- Soporte para cuentas premium y no premium
- Comandos para gestión de cuentas y autenticación
- Captcha para prevenir bots
- Sistema de restablecimiento de contraseñas (PIN CODE)

## Datos de Usuario

El plugin almacena los siguientes datos para cada usuario:

- ID
- Nombre de usuario
- Contraseña (Encriptada)
- Estado de inicio de sesión
- Estado de cuenta premium
- Pin code para restablecimiento de contraseña

## Comandos

El plugin proporciona los siguientes comandos:

1. `/changepw`: Permite a los usuarios cambiar su contraseña
2. `/login`: Inicia sesión en la cuenta
3. `/register`: Registra una nueva cuenta
4. `/logout`: Cierra la sesión actual
5. `/premium`: Gestiona el estado de cuenta premium
6. `/reset`: Restablece la contraseña de la cuenta (Permiso: `bungeeauth.admin`)
7. `/generatepin`: Genera un pin code para restablecimiento de contraseña
8. `/resetpassword`: Restablece la contraseña utilizando el pin code
9. `/forcelogin`: Fuerza el inicio de sesión de un usuario (Permiso: `bungeeauth.admin`)
10. `/authdebug`: Muestra información del jugador (Permiso: `bungeeauth.admin`)

## Licencia y Uso

Este plugin es software libre: puedes redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU...

Copyright (C) 2026 Jose Gambarte