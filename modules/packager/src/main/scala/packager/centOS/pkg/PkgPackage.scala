package packager.centOS.pkg

import packager.BuildSettings
import packager.PackagerUtils.{executablePerms, osWrite}
import packager.centOS.CentOSNativePackager

case class PkgPackage(sourceAppPath: os.Path, buildOptions: BuildSettings)
    extends CentOSNativePackager {

  private val scriptsPath = basePath / "scripts"

  override def build(): Unit = {

    createAppDirectory()
    createInfoPlist()
    createScriptFile()

    os.proc(
        "pkgbuild",
        "--install-location",
        "/Applications",
        "--component",
        s"$packageName.app",
        outputPath,
        "--scripts",
        scriptsPath
      )
      .call(cwd = basePath)

    postInstallClean()
  }

  private def postInstallClean(): Unit = {
    os.remove.all(centOSAppPath)
    os.remove.all(scriptsPath)
  }

  private def createScriptFile(): Unit = {
    val content = s"""#!/bin/bash
                    |rm -f /usr/local/bin/$packageName
                    |ln -s /Applications/$packageName.app/Contents/MacOS/$packageName /usr/local/bin/$packageName""".stripMargin
    os.makeDir.all(scriptsPath)
    val postInstallPath = scriptsPath / "postinstall"
    osWrite(postInstallPath, content, executablePerms)
  }

  override def extension: String = "pkg"
}