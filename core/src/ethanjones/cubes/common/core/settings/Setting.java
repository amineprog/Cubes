package ethanjones.cubes.common.core.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import ethanjones.data.DataGroup;
import ethanjones.data.other.DataParser;

public abstract class Setting implements DataParser<DataGroup> {

  public abstract Actor getActor(VisualSettingManager visualSettingManager);

  public abstract String toString();
}
