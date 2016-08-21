package ethanjones.cubes.input;

import ethanjones.cubes.core.event.entity.living.player.PlayerMovementEvent;
import ethanjones.cubes.core.platform.Compatibility;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.input.keyboard.KeyboardHelper;
import ethanjones.cubes.item.ItemStack;
import ethanjones.cubes.item.ItemTool;
import ethanjones.cubes.networking.NetworkingManager;
import ethanjones.cubes.networking.packets.PacketButton;
import ethanjones.cubes.networking.packets.PacketKey;
import ethanjones.cubes.networking.packets.PacketPlayerMovement;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.collision.PlayerCollision;
import ethanjones.cubes.world.gravity.WorldGravity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.IntIntMap;

public class CameraController extends InputAdapter {

  public static final float JUMP_START_VELOCITY = 5f;
  public static final float JUMP_RELEASE_VELOCITY = 2f;
  public boolean jumping = false;

  public Touchpad touchpad; //movement on android
  public ImageButton jumpButton;

  private final Camera camera;
  private final IntIntMap keys = new IntIntMap();
  private final IntIntMap buttons = new IntIntMap();
  private final Vector3 tmp = new Vector3();
  private final Vector3 tmpMovement = new Vector3();
  private int STRAFE_LEFT = Input.Keys.A;
  private int STRAFE_RIGHT = Input.Keys.D;
  private int FORWARD = Input.Keys.W;
  private int BACKWARD = Input.Keys.S;
  private float speed = 4f;
  private float degreesPerPixel = Settings.getFloatSettingValue(Settings.INPUT_MOUSE_SENSITIVITY) / 3;
  private Vector3 prevPosition = new Vector3();
  private Vector3 prevDirection = new Vector3();

  public CameraController(Camera camera) {
    this.camera = camera;
    camera.position.set(0, 6.5f, 0);
    camera.direction.set(1, 0, 0);
    camera.update();
  }

  @Override
  public boolean keyDown(int keycode) {
    keys.put(keycode, keycode);

    PacketKey packetKey = new PacketKey();
    packetKey.action = PacketKey.KEY_DOWN;
    packetKey.key = keycode;
    NetworkingManager.sendPacketToServer(packetKey);
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    keys.remove(keycode, 0);
    PacketKey packetKey = new PacketKey();
    packetKey.action = PacketKey.KEY_UP;
    packetKey.key = keycode;
    NetworkingManager.sendPacketToServer(packetKey);
    return true;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (Compatibility.get().isTouchScreen()) {
      if (screenX < Gdx.graphics.getWidth() / 3) {
        button = Input.Buttons.LEFT;
      } else if (screenX > Gdx.graphics.getWidth() / 3 * 2) {
        button = Input.Buttons.RIGHT;
      } else {
        return false;
      }
    }
    buttons.put(button, button);
    PacketButton packetButton = new PacketButton();
    packetButton.action = PacketButton.BUTTON_DOWN;
    packetButton.button = button;
    NetworkingManager.sendPacketToServer(packetButton);

    ItemStack itemStack = Cubes.getClient().player.getInventory().selectedItemStack();
    if (itemStack != null)
      itemStack.item.onButtonPress(button, itemStack, Cubes.getClient().player, Cubes.getClient().player.getInventory().hotbarSelected);
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (Compatibility.get().isTouchScreen()) {
      if (screenX < Gdx.graphics.getWidth() / 3) {
        button = Input.Buttons.LEFT;
      } else if (screenX > Gdx.graphics.getWidth() / 3 * 2) {
        button = Input.Buttons.RIGHT;
      } else {
        return false;
      }
    }
    buttons.remove(button, 0);
    PacketButton packetButton = new PacketButton();
    packetButton.action = PacketButton.BUTTON_UP;
    packetButton.button = button;
    NetworkingManager.sendPacketToServer(packetButton);
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return mouseMoved(screenX, screenY);
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (Cubes.getClient().renderer.guiRenderer.noCursorCatching()) return false;
    float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
    float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;

    tmpMovement.set(camera.direction);
    tmpMovement.rotate(camera.up, deltaX);
    tmp.set(tmpMovement).crs(camera.up).nor();
    tmpMovement.rotate(tmp, deltaY);

    if (preventFlicker(tmpMovement)) camera.direction.set(tmpMovement);
    return true;
  }

  private boolean preventFlicker(Vector3 newDirection) {
    float oldX = Math.signum(camera.direction.x);
    float oldZ = Math.signum(camera.direction.z);
    float newX = Math.signum(newDirection.x);
    float newZ = Math.signum(newDirection.z);

    return !(oldX != newX && oldZ != newZ);
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public void update() {
    if (Cubes.getClient().renderer.guiRenderer.noCursorCatching()) {
      update(0f, 0f, 0f, 0f, false);
    } else if (touchpad != null) {
      float knobPercentY = touchpad.getKnobPercentY();
      float up = knobPercentY > 0 ? knobPercentY : 0;
      float down = knobPercentY < 0 ? -knobPercentY : 0;

      float knobPercentX = touchpad.getKnobPercentX();
      float right = knobPercentX > 0 ? knobPercentX : 0;
      float left = knobPercentX < 0 ? -knobPercentX : 0;
      update(up, down, left, right, jumpButton.getClickListener().isPressed());
    } else {
      boolean j = KeyboardHelper.isKeyDown(Input.Keys.SPACE);
      update(keys.containsKey(FORWARD) ? 1f : 0f, keys.containsKey(BACKWARD) ? 1f : 0f, keys.containsKey(STRAFE_LEFT) ? 1f : 0f, keys.containsKey(STRAFE_RIGHT) ? 1f : 0f, j);
    }
  }

  private void update(float forward, float backward, float left, float right, boolean jump) {
    float deltaTime = Gdx.graphics.getRawDeltaTime();
    if (deltaTime == 0f) return;
    tmpMovement.setZero();
    if (forward > 0) {
      tmp.set(camera.direction.x, 0, camera.direction.z).nor().nor().scl(deltaTime * speed * forward);
      tmpMovement.add(tmp);
    }
    if (backward > 0) {
      tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-deltaTime * speed * backward);
      tmpMovement.add(tmp);
    }
    if (left > 0) {
      tmp.set(camera.direction.x, 0, camera.direction.z).crs(camera.up).nor().scl(-deltaTime * speed * left);
      tmpMovement.add(tmp);
    }
    if (right > 0) {
      tmp.set(camera.direction.x, 0, camera.direction.z).crs(camera.up).nor().scl(deltaTime * speed * right);
      tmpMovement.add(tmp);
    }
    if (!tmpMovement.isZero()) tryMove();

    if (!jumping && jump && WorldGravity.onBlock(Cubes.getClient().world, Cubes.getClient().player.position, Player.PLAYER_HEIGHT, PlayerCollision.r)) {
      Cubes.getClient().player.motion.y = JUMP_START_VELOCITY;
      jumping = true;
    }
    if (jumping && !jump) {
      Cubes.getClient().player.motion.y = Math.min(JUMP_RELEASE_VELOCITY, Cubes.getClient().player.motion.y);
      jumping = false;
    }
    if (Cubes.getClient().player.motion.y <= 0) jumping = false;

    Cubes.getClient().player.updatePosition(deltaTime);

    camera.update(true);
  }

  private void tryMove() {
    tmpMovement.add(camera.position);
    if (!new PlayerMovementEvent(Cubes.getClient().player, tmpMovement).post().isCanceled()) {
      camera.position.set(tmpMovement);
    }
  }

  public void tick() {
    Player player = Cubes.getClient().player;
    if (!player.position.equals(prevPosition) || !player.angle.equals(prevDirection)) {
      NetworkingManager.sendPacketToServer(new PacketPlayerMovement(player));
      prevPosition.set(player.position);
      prevDirection.set(player.angle);
    }
    ItemTool.mine(player, buttons.containsKey(Input.Buttons.LEFT));
  }
}

