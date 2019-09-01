package discordbot.sabacan;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;


public class SabacanBot {

	private static final Map<String, Command> commands = new HashMap<>();
	private static DiscordClientBuilder discordClientBuilder = new DiscordClientBuilder("NTc2NjI2ODg1MDgxOTU2MzUz.XNZtEQ.H0EoY-Xu2Vps9ydKHhXZefIfIJY");
	private static String ID = "576626885081956353";
    private static DiscordClient discordClient = discordClientBuilder.build();

    public static void main(String[] args) {

    	MinecraftService.registCommand(commands);
    	SevenDaysToDieService.registCommand(commands);

		commands.put("help", event -> {
			MessageChannel channel = event.getMessage().getChannel().block();
			channel.createMessage(""
					+ "--- 鯖缶ちゃん 説明書---\n"
					+ "色んなゲームのマルチサーバをdiscordから操作するためのBOTだよ。いつでも呼びかけてね(*‘∀‘)\n"
					+ "※注意：鯖缶ちゃんは生まれつき身体が弱いため、乱暴に扱うと動かなくなります。\n"
					+ "　　　　コマンドをうったら、鯖缶ちゃんからレスポンスがあるまで待ちましょう。\n"
					+ "------\n"
					+ "基本的に「@鯖缶ちゃん (コマンド)」のように入力して、discordのメンション機能でBOTに呼びかけるようにして使います。\n"
					+ "マイクラ情報を出力するコマンド例「@鯖缶ちゃん minecraft info」\n\n"
					+ "各ゲームの使い方は以下のコマンドで確認できます。\n"
					+ "minecraft help ・・・ マインクラフトに関する使い方を表示。\n"
					+ "7dtd help ・・・ 7DaysToDieに関する使い方を表示。\n"
					+ "------\n"
					+ "サーバ起動中はお金がかかっちゃうので、長時間プレイしない時は停止しておいてね！(＞人＜;)\n").block();
		});

        discordClient.getEventDispatcher().on(MessageCreateEvent.class)
        	.subscribe(event -> {
        		final String content = event.getMessage().getContent().orElse("");

        		for(final Map.Entry<String, Command> entry : commands.entrySet()){
            		if(content.startsWith("<@" + ID + "> " + entry.getKey())){
        				entry.getValue().execute(event);
        				break;
        			}
        		}
        	});
        discordClient.login().block();
    }
}