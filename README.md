## Me, playing around with Scala 3

I'm trying to use as many fancy keywords that I can. Here it is what I've gotten so far:

A purely functional asynchronous streamed real time websocket discord bot that uses scala 3, cats and fs2.

### Usage

Create a file named **config.yaml** at project root dir with the following content:
```yaml
token: [PUT YOUR BOT TOKEN HERE]
```

And then run:
``` sh
sbt runWithProxy
```

It needs docker and port 8020 opened.
