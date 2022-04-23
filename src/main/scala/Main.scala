import org.javacord.api.DiscordApiBuilder
import org.javacord.api.interaction.SlashCommand

import scala.io.Source

@main def hello: Unit = {

  val token = Source.fromResource("token.txt").getLines().nextOption() match {
      case Some(text) => text
      case None => throw Exception("No token found")
    }

  val api = DiscordApiBuilder().setToken(token).login().join()
  SlashCommand.`with`("ping", "a simple ping pong command!").createGlobal(api).join()

  println("Hello world!")
  println(msg)
}

def msg = "I was compiled by Scala 3. :)"
