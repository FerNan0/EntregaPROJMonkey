package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class ProjetoM2 extends SimpleApplication 
                        implements ActionListener,
                        PhysicsCollisionListener{

    private TerrainQuad terrain;
    Material matRock;
    Material matWire;
    boolean wireframe = false;
    boolean triPlanar = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;
    private float speed = 128;
    boolean restart = false, resume = true;    
    int pontuacaoFinal;
    long t;
    DirectionalLight l1,l2,l3,l4;
    AmbientLight ambient;
    public static void main(String[] args) {
        ProjetoM2 app = new ProjetoM2();
        app.showSettings = false;
        app.start();
        
    }
    
    private BulletAppState bulletAppState;
    private PlayerCameraNode player;
    private boolean up = false, down = false, left = false, right = false;
    private Material boxMatColosion;
    private int count = 0;    
    private long time = System.currentTimeMillis();
    private float velox = 3;
    private int pontuacaoAux = 0;
    private long timePlaying = System.currentTimeMillis();
    
    @Override
    public void initialize() {
        super.initialize();        
        loadHintText();
    }
    
    public void loadHintText() {
        hintText = new BitmapText(guiFont, false);
        hintText.setSize(guiFont.getCharSet().getRenderedSize());
        hintText.setLocalTranslation(0, getCamera().getHeight(), 0);        
        guiNode.attachChild(hintText);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        resume = true;
        
        player.upDateKeys(tpf, up, down, left, right, velox);
        guiNode.detachAllChildren();

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        t = (System.currentTimeMillis()/1000) - (timePlaying/1000);
        
        if(t >= 30) {
            resume = false;
            guiNode.detachAllChildren();
            rootNode.detachAllChildren();
            rootNode.removeLight(pl);
            rootNode.removeLight(l1);
            rootNode.removeLight(l2);
            rootNode.removeLight(l3);
            rootNode.removeLight(l4);
            rootNode.removeLight(ambient);
            rootNode.detachAllChildren();
            if(count > - 500000) {
                pontuacaoFinal = count;
            }
            count = -1000000000;
            
          if(restart) {
            restart = false;
            simpleInitApp();
            timePlaying = System.currentTimeMillis();            
            count = 0;
            pontuacaoAux = 0;
            velox = 3;                 
          } else {
              
                BitmapText textoFinal = new BitmapText(guiFont, false);                
                
                    textoFinal.setSize(guiFont.getCharSet().getRenderedSize());

                    textoFinal.setText("Pontuacao Final: " + pontuacaoFinal + "\n\n Precione espaco para jogar novamente!");

                    textoFinal.setLocalTranslation(200, textoFinal.getLineHeight() + 250, 0);                        

                    guiNode.attachChild(textoFinal);
            
          }
         
        } else {
                             
            BitmapText helloText = new BitmapText(guiFont, false);

            helloText.setSize(guiFont.getCharSet().getRenderedSize());

            helloText.setText("         Pontuacao: " + count + "     Tempo: " + t);

            helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);

            guiNode.attachChild(helloText);

        if(time + 180 < System.currentTimeMillis() && resume) {
           time = System.currentTimeMillis();
           
           Random r = new Random();
           createBoxes(r.nextInt(50));
        }
        
        }
        
        if(restart) restart = false;
    }

    private void setupKeys() {
        flyCam.setMoveSpeed(50);
    }
    
    @Override
    public void simpleInitApp() {
        
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);
        stateManager.attach(bulletAppState);
         setupKeys();
         createScene();
         createPlayer();    
         criarLuzes();
         
         initKeys();
         bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }
    
    private void initKeys() {
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addListener(this, "CharLeft", "CharRight");
        inputManager.addListener(this, "CharForward", "CharBackward");
        inputManager.addListener(this, "Restart");

    }
    
     private void criarLuzes() {

        l1 = new DirectionalLight();
        l1.setDirection(new Vector3f(1, -0.7f, 0));
        rootNode.addLight(l1);

        l2 = new DirectionalLight();
        l2.setDirection(new Vector3f(-1, 0, 0));
        rootNode.addLight(l2);

        l3 = new DirectionalLight();
        l3.setDirection(new Vector3f(0, 0, -1.0f));
        rootNode.addLight(l3);

        l4 = new DirectionalLight();
        l4.setDirection(new Vector3f(0, 0, 1.0f));
        rootNode.addLight(l4);


        ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);


    }
    
    private void createBoxes(int aux) {
        Random r = new Random();
        float x = (r.nextInt(10) - 15) + player.getLocalTranslation().x, y = (r.nextInt(10) + 5) + player.getLocalTranslation().y, z = player.getLocalTranslation().z;
        
        /* A colored lit cube. Needs light source! */
        Box boxMesh = new Box(0.5f,0.5f,0.5f);//(Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");                        
        Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        boxMat.setBoolean("UseMaterialColors", true);
        Geometry boxGeo;
        
        if(aux > count/300) {
            boxGeo = new Geometry("BoxGreen", boxMesh);        
            boxMat.setColor("Ambient", ColorRGBA.Green);
            boxMat.setColor("Diffuse", ColorRGBA.Green);
        } else {
            boxGeo = new Geometry("BoxRed", boxMesh);        
            boxMat.setColor("Ambient", ColorRGBA.Red);
            boxMat.setColor("Diffuse", ColorRGBA.Red);
        }
        
        boxGeo.setMaterial(boxMat);
        boxGeo.setLocalTranslation(x,y,z);
        rootNode.attachChild(boxGeo);        

        RigidBodyControl boxPhysicsNode = new RigidBodyControl(1);
        boxGeo.addControl(boxPhysicsNode);
        bulletAppState.getPhysicsSpace().add(boxPhysicsNode);
    }
    
    private void createPlayer() {
        player = new PlayerCameraNode("player", assetManager, bulletAppState, cam);
        rootNode.attachChild(player);
        flyCam.setEnabled(false);
    }
    
    private void createScene() {
        // TERRAIN TEXTURE material
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);

        // ALPHA map (for splat textures)
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", grassScale);

        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", dirtScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", rockScale);

        // WIREFRAME material
        matWire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matWire.getAdditionalRenderState().setWireframe(true);
        matWire.setColor("Color", ColorRGBA.Green);

        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        /**
         * Optimal terrain patch size is 65 (64x64).
         * The total size is up to you. At 1025 it ran fine for me (200+FPS), however at
         * size=2049, it got really slow. But that is a jump from 2 million to 8 million triangles...
         */
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        control.setLodCalculator( new DistanceLodCalculator(65, 2.7f) ); // patch size, and a multiplier
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 0.5f, 2f);
        
       
        
        RigidBodyControl terrerainPhysicsNode = new RigidBodyControl(CollisionShapeFactory.createMeshShape(terrain), 0);        
        bulletAppState.getPhysicsSpace().add(terrerainPhysicsNode);
        terrain.addControl(terrerainPhysicsNode);
        rootNode.attachChild(terrain);

        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
        rootNode.addLight(light);

        cam.setLocation(new Vector3f(0, 10, -10));
        cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);
    }
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean value, float tpf) {            
               
        }
    };

    @Override
    public void onAction(String binding, boolean value, float tpf) {
            
        switch (binding) {
            case "CharLeft":
                if (value) {
                    left = true;
                } else {
                    left = false;
                }
                break;
            case "CharRight":
                if (value) {
                    right = true;
                } else {
                    right = false;
                }
                break;
            case "Restart" :
                if(t >= 30) restart = true;
                break;
        }
        switch (binding) {
            case "CharForward":
                if (value) {
                    up = true;
                } else {
                    up = false;
                }
                break;
            case "CharBackward":
                if (value) {
                    down = true;
                } else {
                    down = false;
                }
                break;
        }

    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        
        if(pontuacaoAux > 500) {
            velox += 3;
            pontuacaoAux = 0;
        }
        
        if(event.getNodeA().getName().equals("player") || 
           event.getNodeB().getName().equals("player")){
            
            if(event.getNodeA().getName().equals("BoxGreen")){                
                if(resume) {
                    rootNode.detachChild(event.getNodeA());                    
                    count++;
                    pontuacaoAux++;
                }
            }
            else
            if(event.getNodeB().getName().equals("BoxGreen")){
                if(resume) {
                    rootNode.detachChild(event.getNodeB());
                    count++;
                    pontuacaoAux++;
                }
            }
            else
            if(event.getNodeA().getName().equals("BoxRed")){                
                if(resume) {
                    rootNode.detachChild(event.getNodeA());
                    count-=3;
                    pontuacaoAux = 0;
                    velox = 3;
                }
            }
            else
            if(event.getNodeB().getName().equals("BoxRed")){
                if(resume) {
                    rootNode.detachChild(event.getNodeB());
                    count-=3;
                    pontuacaoAux = 0;
                    velox = 3;
                }
            }
            
        }
    }
    
}
