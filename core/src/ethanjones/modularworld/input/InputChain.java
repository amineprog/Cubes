package ethanjones.modularworld.input;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class InputChain extends InputMultiplexer {
  
  public Stage hud;
  public GameInputHandler game;
  
  public InputChain() {
    super();
  }
  
  public InputChain init() {
    this.addProcessor(hud);
    this.addProcessor(game = new GameInputHandler());
    return this;
  }
  
}
