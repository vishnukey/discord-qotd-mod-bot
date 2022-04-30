A project to create a discord bot designed to help automate a question of the day via allowing anonymous submissions of questions, moderator approval of questions for queueing, and automated posting of the questions at a specific indicated time of day, creating a new thread in the indicated channel for each daily question.

## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

## Project Plan

### Commands

/qotd <QuestionInput:String>

    /config 
 
        /set <ChannelName:Mentionable>
            -verification channel
            -qotd channel
            -time   

        /make
            -verification channel
            -qotd channel

should present choices on if it'll be the 

    /submit
received by the bot, sent to the verification
respond to the emojis being clicked
take appropriate action


pops the oldest message in verification over to qotd channel
    -creates a thread? allow config to toggle if creating a thread or just a message that's pinned
    -