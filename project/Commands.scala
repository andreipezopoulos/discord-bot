import scala.sys.process._

object Commands {
    private val dockerName = "proxy-to-discord"

    val dockerPs =
        s"""docker ps --filter name=${dockerName} --format "{{.ID}}" """

    val dockerRm =
        s"docker rm -f ${dockerName}" #| "echo Stopping discord proxy..."

    def dockerRun = {
        val abspath = new java.io.File("nginx.conf").getAbsolutePath()
        s"""docker run --rm --name ${dockerName} -v ${abspath}:/etc/nginx/nginx.conf:ro -d -p 8020:8020 nginx """
    }
}
