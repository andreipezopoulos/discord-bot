package discord.helper

object Payload:

    val gatewayHello = """
{
  "t" : null,
  "s" : null,
  "op" : 10,
  "d" : {
    "heartbeat_interval" : 7000
  }
}
"""

    val heartBeat = """
{
    "op": 11
}
"""

    def anyRequest = """
{
    "s": null,
    "t": "ANYTHING",
    "op": 999,
    "d": null
}
"""

    def reconnect = """
{
    "op": 7,
    "d": null
}
"""

    def ready = """
{
    "s": null,
    "t": "READY",
    "op": 0,
    "d": {
        "v": 123,
        "session_id": "abdce",
        "shard": [],
        "guilds": [],
        "application": {},
        "user": {}
    }
}
"""

    def resume = """
{
    "t" : "RESUMED",
    "s" : 2,
    "op" : 0,
    "d" : {
        "_trace": []
    }
}
"""

    def reset = """
{
    "op": 9,
    "d": false
}
"""

end Payload
