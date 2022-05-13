import scala.io.Source
import org.javacord.api.*
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.interaction.{ApplicationCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionType}

import scala.jdk.javaapi.CollectionConverters
import scala.jdk.OptionConverters.*
import scala.jdk.CollectionConverters.*

@main def hello: Unit = {

  val (api, slashcommands):(DiscordApi, Map[Long, Option[Command]]) = initializeStartup()


  //message listener attributes
  api.addMessageCreateListener(event => {
    event.getMessageContent match
      case "!ping" => doPing(event)
      case "!help" => doHelpSummary(event)
      case _ => event.getChannel.sendMessage("Invalid Command")
  })

  api.addSlashCommandCreateListener(event => {
    val interaction = event.getSlashCommandInteraction
    slashcommands.get(interaction.getCommandId).flatten match {
      case Some(command) => command.handler(interaction)
      case None => {}
    }
  })
}

def initializeStartup(): (DiscordApi, Map[Long, Option[Command]]) = {
  val token = Source.fromResource("token.txt").getLines().nextOption() match {
    case Some(text) => text
    case None => throw Exception("No token found")
  }

  val api = new DiscordApiBuilder().setToken(token).login.join
  val commands = initializeCommands(api)
  val globalCommands = api.getGlobalSlashCommands.join
  if (globalCommands.size() == 0 || globalCommands.size() != commands.size) {
      api.bulkOverwriteGlobalApplicationCommands(CollectionConverters.asJava(commands.map(_.slashCommand)))
  }

  val commandIdMap = api.getGlobalSlashCommands.join.asScala.toSeq.map(x => (x.getId, x.getName)).toMap
  val commandNameMap = commands.map(x => (x.name, x)).toMap
  val commandMap = commandIdMap.map((id, name) => (id, commandNameMap.get(name)))
  (api, commandMap)
}

// Add a listener which answers with "Pong!" if someone writes "!ping"
def doPing(event : MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("Pong!")
}

def doHelpSummary(event: MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("insert summary of commands here.")
}

  /*
  * Bulk writing commands, as advised to ideally only do 1 request
  */
def initializeCommands(api: DiscordApi/*, commands: Seq[Command]*/): List[Command] = {
//  List(
//    SlashCommandBuilder().setName("server").setDescription("A command for the server")
//    )
  //List(Command("server", "a command for the server"))
    val commands = List(
      Command("server", "a command for the server"),
      Command(
        "test",
        "a command with arguments",
        Seq(
          SlashCommandOption.create(SlashCommandOptionType.STRING, "echo", "to be echoed"),
          SlashCommandOption.create(SlashCommandOptionType.STRING, "private", "won't be echoed")
        ),
        interaction => {
          val args = interaction.getArguments
          val echo = args.get(0).getStringValue.toScala
          val priv = args.get(1).getStringValue.toScala
          println(s"private messatge from test ${priv}")
          interaction.createImmediateResponder().setContent(s"echoing: ${echo}").respond()
        }
      )
    )
  commands

//  val commands = List(
//    SlashCommandBuilder().setName("server").setDescription("A command for the server")
//  )
//
//  api.bulkOverwriteGlobalApplicationCommands(CollectionConverters.asJava(commands)).join()
//  val commands = Seq(
//    Command("server", "a command for the server")
//  )
//  api.bulkOverwriteGlobalApplicationCommands(
//    CollectionConverters.asJava(commands.map(_.slashCommand))
//  )
//  api.addSlashCommandCreateListener(event => {
//    val interaction = event.getSlashCommandInteraction
//    val commandName = interaction.getCommandName
//    commands.filter(_.name == commandName).foreach(_.handler(interaction))
//  })
}

class Command(
               val name:String,
               val description: String = "",
               val options: Seq[SlashCommandOption] = Seq(),
               val handler: (SlashCommandInteraction => Unit) = _ => {}){
  val slashCommand = SlashCommandBuilder()
    .setName(name)
    .setDescription(description)
    .setOptions(CollectionConverters.asJava(options))
}