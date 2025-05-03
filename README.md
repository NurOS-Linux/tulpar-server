<h1 align="center">Tulpar Server 2.0</h1><br>

<p align="center">
  <a href="https://nuros.org">
    <img src="https://avatars.githubusercontent.com/u/183817345?s=200&v=4" alt="nuros logo" height="140">
  </a>
</p>

<p align="center">
  TulparServer® is a free, open-source, cross-platform Java Server<br>
  Create your own NurOS package repository in minutes.
</p>

----
<br>
<div align="center">

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)


Server repository

[Features](#features) •
[Usage](#usage) •
[Configuration](#configuration) •
[Contributing](#contributing) •
[Build](#build) •
[Roadmap](#roadmap)

</div>

## Features

🚀 **High Performance**  
- Optimised for low‑end devices  
- Requires only 128 MB RAM (plus ~256 MB for plugins)

🔄 **Cross‑Platform**  
- Runs on any JVM (Linux, Android, macOS, Windows)

🔌 **Plugin Ecosystem**  
- Easy plugin install/uninstall via CLI  
- Auto‑discovery of plugins on startup

🛡️ **Security‑First**  
- HTTPS support out of the box  
- Role‑based access controls

### Usage

```bash
# Start a server
java -jar server.jar
# Or run in Docker
docker run -p 8080:8080 nuros/tulpar-server
```

## Configuration

Create `config.json` in the working directory:

```json
{
	"api": {
		"config": 11
	},
	"server": {
		"address": "0.0.0.0",
		"port": 80,
		"runInBackground": "true",
		"maxRequests": 5,
		"blockDurationMillis": 60000,
		"logFile": "./ip_requests.log",
		"httpsRedirect": true,
		"maxDownloadsPerIP": 2,
		"maxDownloadSpeed": 1048576,
		"BUFFER_SIZE": 1024
	},
	"cli": {
		"color": "bright_red",
		"hello": [
			"▀█▀ █░█ █░░ █▀█ ▄▀█ █▀█ ▄▄ █▀ █▀▀ █▀█ █░█ █▀▀ █▀█   ▀█ ░ █▀█",
			"░█░ █▄█ █▄▄ █▀▀ █▀█ █▀▄ ░░ ▄█ ██▄ █▀▄ ▀▄▀ ██▄ █▀▄   █▄ ▄ █▄█"
		]
	}
}
```


## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md).

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Build

Requirements:

* JDK 8+ (Zulu or any distribution)
* Jetty, Jansi, JNA, Gson, Jython, jna‑platform, and others

```bash
git clone https://github.com/nuros-linux/tulpar-server.git
cd tulpar-server
./gradlew clean build
```

## Roadmap

* ⚙️ Better CLI commands
* 📦 Official plugin registry

## FAQ

**Q**: Can I run on ARM devices?  
**A**: Yes, any device with a compatible JVM works.

**Q**: What package formats and metadata are supported by default?  
**A**: The primary package format is `example.apg`. The detailed structure and metadata schema are described in the [APGexample repository](https://github.com/NurOS-Linux/APGexample)

**Q**: Where are server logs stored and how can I change the log level?  
**A**: Depending on the Tulpar Server version, logs are either in the OS temporary folder or in the program’s root directory. In some versions, you can adjust the log level in your `config.json`.

**Q**: How can I update the server to a new version without downtime?  
**A**: An `update` command is planned for upcoming releases, but currently there’s no hot‑update feature—you must stop the server, deploy the new build, and restart.

**Q**: Can I run multiple Tulpar Server instances behind a load balancer?  
**A**: Cluster mode and load‑balancing support will be introduced in future versions.

**Q**: How do I restrict access to specific packages by roles or groups?  
**A**: Role‑based access control for packages is coming in upcoming releases.

**Q**: What JVM parameters can I tweak for better performance?  
**A**: Recommended JVM arguments:  
```

-Xms512m                           # initial heap size
-Xmx2g                             # maximum heap size
-XX:+UseG1GC                       # G1 Garbage Collector for low pause times
-XX\:MaxGCPauseMillis=200           # target max GC pause time
-XX\:ParallelGCThreads=<numCores>   # GC thread count
-XX:+UnlockExperimentalVMOptions   # enable experimental options

```
Replace `<numCores>` with your CPU core count to utilize all threads and optimize memory usage and GC pauses.


## Support

* [Issues](https://github.com/nuros-linux/tulpar-server/issues)
* [Discussions](https://github.com/nuros-linux/tulpar-server/discussions)

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
