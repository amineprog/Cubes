package ethanjones.modularworld.graphics.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ethanjones.modularworld.graphics.GraphicsHelper;
import ethanjones.modularworld.input.keyboard.KeyTypedListener;
import ethanjones.modularworld.input.keyboard.KeyboardHelper;
import ethanjones.modularworld.networking.NetworkingManager;
import ethanjones.modularworld.networking.packets.PacketChat;
import ethanjones.modularworld.side.client.ClientDebug;
import ethanjones.modularworld.side.client.ModularWorldClient;

import static ethanjones.modularworld.graphics.menu.Menu.skin;

public class HudRenderer implements Disposable {

  private class KeyListener implements KeyTypedListener {

    final int debug = Input.Keys.F1;
    final int chat = Input.Keys.F2;

    boolean debugDown = false;
    boolean chatDown = false;

    @Override
    public void keyDown(int keycode) {
      if (keycode == debug && !debugDown) {
        debugDown = true;
        setDebugEnabled(!isDebugEnabled());
      }
      if (keycode == chat && !chatDown) {
        chatDown = true;
        setChatEnabled(!isChatEnabled());
      }
    }

    @Override
    public void keyUp(int keycode) {
      if (keycode == debug) debugDown = false;
      if (keycode == chat) chatDown = false;
    }

    @Override
    public void keyTyped(char character) {

    }
  }

  private boolean chatEnabled; //TODO: On screen buttons
  private boolean debugEnabled;

  Stage stage;
  TextField chat;
  ClientDebug.DebugLabel debug;

  public HudRenderer() {
    stage = new Stage(new ScreenViewport());
    ModularWorldClient.instance.inputChain.hud = stage;

    debug = new ClientDebug.DebugLabel();

    TextField.TextFieldStyle defaultStyle = skin.get("default", TextField.TextFieldStyle.class);
    TextField.TextFieldStyle chatStyle = new TextField.TextFieldStyle(defaultStyle);
    chatStyle.background = new TextureRegionDrawable(GraphicsHelper.getTexture("hud/ChatBackground.png").textureRegion);

    chat = new TextField("", chatStyle);
    chat.setTextFieldListener(new TextField.TextFieldListener() {
      @Override
      public void keyTyped(TextField textField, char c) {
        if (c == '\n' || c == '\r') {
          PacketChat packetChat = new PacketChat();
          packetChat.msg = chat.getText();
          NetworkingManager.clientNetworking.sendToServer(packetChat);
          chat.setText("");
          setChatEnabled(false);
        }
      }
    });
    KeyboardHelper.addKeyTypedListener(new KeyListener());

    setChatEnabled(false);
    setDebugEnabled(false);
  }

  public void render() {
    stage.clear();
    if (isDebugEnabled()) stage.addActor(debug);
    if (isChatEnabled()) {
      stage.addActor(chat);
      stage.setKeyboardFocus(chat);
    }
    stage.act();
    stage.draw();
  }

  public void resize() {
    stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    chat.setBounds(0, 0, Gdx.graphics.getWidth(), chat.getStyle().font.getBounds("ABC123").height * 1.5f);
  }

  @Override
  public void dispose() {
    stage.dispose();
  }

  public boolean isChatEnabled() {
    return chatEnabled;
  }

  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  public void setChatEnabled(boolean chatEnabled) {
    this.chatEnabled = chatEnabled;
  }

  public void setDebugEnabled(boolean debugEnabled) {
    if (debugEnabled) {
      GLProfiler.disable();
    } else {
      GLProfiler.enable();
    }
    this.debugEnabled = debugEnabled;
  }
}
