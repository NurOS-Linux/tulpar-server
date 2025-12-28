<div align="center">

<h1 align="center">Tulpar Server 2.0</h1>

<p align="center">
  <a href="https://nuros.org">
    <img src="https://avatars.githubusercontent.com/u/183817345?s=200&v=4" alt="nuros logo" height="140">
  </a>
</p>

**TulparServer** is a free, open-source, cross-platform Java Server designed to create your own **NurOS** package repository in minutes.
It features high performance, security-first architecture, and a modern CLI interface.

[![](https://img.shields.io/badge/License-GPL%203.0-blue.svg?style=flat-square)](https://www.gnu.org/licenses/gpl-3.0)
[![](https://img.shields.io/github/issues/nuros-linux/tulpar-server?style=flat-square&logo=github)](https://github.com/nuros-linux/tulpar-server/issues)

</div>

<br>

<div align="center">
  <img src="https://github.com/user-attachments/assets/f66c2629-7b93-4afe-b024-b63fc36a32d1" alt="Tulpar Server Interface" width="80%">
</div>
<br>

## 🔧 Build info

**Requirements:** JDK 21+

#### Build
[![](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/gradle_vector.svg)](https://gradle.org/)

`./gradlew build`: Builds all jars <br>
`./gradlew shadowJar`: Builds fat-jar with all dependencies (recommended) <br>
`./gradlew test`: Runs unit tests <br>

#### Libraries
- [Ktor](https://ktor.io/): Asynchronous framework for creating microservices and web applications
- [Clikt](https://ajalt.github.io/clikt/): Intuitive command line interface parsing
- [Mordant](https://github.com/ajalt/mordant): Full-featured text styling for the terminal
- [Hoplite](https://github.com/sksamuel/hoplite): Boilerplate-free configuration library (HOCON)
- [Logback](https://logback.qos.ch/): Robust logging framework

## Features

*Will appear after the development of 2.0 is completed.*

## Usage

```bash
# Start the server using the shadow jar
java -jar tulpar-server-2.0.0-all.jar start

# Run with custom config
java -jar tulpar-server-2.0.0-all.jar start --config /path/to/application.conf

# Show help
java -jar tulpar-server-2.0.0-all.jar --help
```

## Configuration

Tulpar Server uses HOCON for configuration. Create `application.conf` in the working directory:
```hocon
api {
    config = 20
}

server {
    address = "0.0.0.0"
    port = 8080 # для портов ниже 1024 нужен root

    runInBackground = true

    maxRequests = 5
    blockDurationMillis = 60000

    logFile = "./ip_requests.log"

    httpsRedirect = true

    # Лимит загрузок на один IP
    maxDownloadsPerIP = 2

    # Максимальная скорость загрузки (байт/с)
    maxDownloadSpeed = 1048576

    # Размер буфера
    bufferSize = 1024
}

cli {
    color = "#cccccc"
    hello = [
        "▀█▀ █░█ █░░ █▀█ ▄▀█ █▀█ ▄▄ █▀ █▀▀ █▀█ █░█ █▀▀ █▀█   ▀█ ░ █▀█",
        "░█░ █▄█ █▄▄ █▀▀ █▀█ █▀▄ ░░ ▄█ ██▄ █▀▄ ▀▄▀ ██▄ █▀▄   █▄ ▄ █▄█"
    ]
}
```

## Contributing

We welcome contributions!
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Roadmap
[ ] 🛠 Core Architecture Rewrite (v2.0) <br>
[ ] 📦 Official Plugin Registry

## FAQ

Will appear after the development of 2.0 is completed.