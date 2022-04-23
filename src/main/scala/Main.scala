import scala.io.Source
import org.javacord.api.*
import org.javacord.api.event.message.MessageCreateEvent

@main def hello: Unit = {

  val token = Source.fromResource("token.txt").getLines().nextOption() match {
      case Some(text) => text
      case None => throw Exception("No token found")
    }

  val api = new DiscordApiBuilder().setToken(token).login.join

  // Add a listener which answers with "Pong!" if someone writes "!ping"
  api.addMessageCreateListener((event: MessageCreateEvent) => {
    def pingpong(event: MessageCreateEvent) = if (event.getMessageContent.equalsIgnoreCase("!ping")) event.getChannel.sendMessage("Pong!")
    pingpong(event)
  })
}

def msg = "I was compiled by Scala 3. :)"
