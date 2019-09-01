package discordbot.sabacan;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteShellExecutor implements Closeable {

  private Session session;

  private ChannelExec channel;

  /**
   * コンストラクタ
   * @param host
   * @param userName
   * @param port
   * @throws Exception
   */
  public RemoteShellExecutor (String host, String userName, int port) {
    try {
      JSch jsch = new JSch();
      jsch.addIdentity("~/.ssh/lynlynkey.pem");
      session = jsch.getSession(userName, host, port);
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();
      channel = (ChannelExec)session.openChannel("exec");
    } catch (JSchException e) {
      // 例外時の処理
    }
  }

    /**
     * コマンドを実行する。
     * @param command
     * @return 処理結果
     * @throws IOException
     * @throws JSchException
     */
    public int execute(String command) throws Exception {
      // コマンド実行する。
      this.channel.setCommand(command);
      channel.connect();
      // エラーメッセージ用Stream
        BufferedInputStream errStream = new BufferedInputStream(channel.getErrStream());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int len = errStream.read(buf);
            if (len <= 0) {
                break;
            }
            outputStream.write(buf, 0, len);
        }
        // エラーメッセージ取得する
        //String message = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        channel.disconnect();
        // コマンドの戻り値を取得する
        int returnCode = channel.getExitStatus();
        return  returnCode;
    }

  @Override
  public void close() {
    session.disconnect();
  }
}
