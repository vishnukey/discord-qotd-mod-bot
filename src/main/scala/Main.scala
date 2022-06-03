import scala.io.Source
import org.javacord.api.*
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.interaction.{ApplicationCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionType}

import scala.jdk.javaapi.CollectionConverters
import scala.jdk.OptionConverters.*
import scala.jdk.CollectionConverters.*

object Main {
  @main def main: Unit = {
    val token = Source.fromResource("token.txt").getLines().nextOption() match {
      case Some(text) => text
      case None => throw Exception("No token found")
    }
    val api = new DiscordApiBuilder().setToken(token).login.join
  }
}