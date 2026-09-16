package nl.leobode.sprookjestuin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;
import java.util.Random;

public final class FairyGardenView extends View {
    private final GardenEngine engine = new GardenEngine();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF evolve = new RectF(), switchMode = new RectF(), reset = new RectF();
    private final RectF[] cards = new RectF[GardenEngine.POPULATION];
    private final Random random = new Random(7);
    private boolean gardenMode;
    private long start = SystemClock.uptimeMillis();

    public FairyGardenView(Context c) { super(c); for(int i=0;i<cards.length;i++)cards[i]=new RectF(); }

    @Override protected void onDraw(Canvas c) {
        float t=(SystemClock.uptimeMillis()-start)/1000f;
        drawSky(c);
        if(gardenMode) drawGarden(c,t); else drawSelection(c,t);
        drawTop(c); drawButtons(c);
        postInvalidateOnAnimation();
    }

    private void drawSky(Canvas c) {
        p.setShader(new LinearGradient(0,0,0,getHeight(),Color.rgb(17,27,53),Color.rgb(67,71,91),Shader.TileMode.CLAMP));
        c.drawRect(0,0,getWidth(),getHeight(),p); p.setShader(null);
        p.setColor(Color.argb(80,214,190,255));
        for(int i=0;i<22;i++) { float x=(i*193)%getWidth(), y=dp(35)+(i*71)%Math.max(1,(int)(getHeight()*.55f)); c.drawCircle(x,y,dp(i%3==0?1.8f:1f),p); }
    }

    private void drawTop(Canvas c) {
        p.setColor(Color.WHITE); p.setTextSize(dp(25)); p.setFakeBoldText(true); c.drawText("Sprookjestuin",dp(20),dp(38),p); p.setFakeBoldText(false);
        p.setColor(Color.rgb(220,203,234)); p.setTextSize(dp(13));
        c.drawText(gardenMode?"De bloemen die je door de generaties heen bewaarde":String.format(Locale.getDefault(),"Generatie %d  •  kies maximaal drie bloemen",engine.generation),dp(20),dp(61),p);
    }

    private void drawSelection(Canvas c,float t) {
        boolean landscape=getWidth()>getHeight(); int cols=landscape?4:2, rows=landscape?2:4;
        float top=dp(78), bottom=getHeight()-dp(82), gap=dp(10), margin=dp(14);
        float w=(getWidth()-margin*2-gap*(cols-1))/cols, h=(bottom-top-gap*(rows-1))/rows;
        for(int i=0;i<GardenEngine.POPULATION;i++) {
            int col=i%cols,row=i/cols; float l=margin+col*(w+gap),tt=top+row*(h+gap); cards[i].set(l,tt,l+w,tt+h);
            p.setColor(engine.selected[i]?Color.argb(165,112,74,139):Color.argb(95,255,255,255)); c.drawRoundRect(cards[i],dp(18),dp(18),p);
            float cx=cards[i].centerX(), ground=cards[i].bottom-dp(16), scale=Math.min(w,h)/dp(165);
            drawFlower(c,engine.candidates.get(i),cx,ground,scale,engine.selected[i],t+i);
            if(engine.selected[i]) { p.setColor(Color.WHITE); p.setTextSize(dp(18)); c.drawText("✦",l+dp(11),tt+dp(23),p); }
        }
    }

    private void drawGarden(Canvas c,float t) {
        float ground=getHeight()-dp(82); p.setColor(Color.rgb(27,63,48)); c.drawRect(0,ground-dp(25),getWidth(),ground,p);
        if(engine.garden.isEmpty()) { p.setColor(Color.rgb(225,210,236)); p.setTextSize(dp(17)); p.setTextAlign(Paint.Align.CENTER); c.drawText("Je tuin wacht op haar eerste gekozen bloemen",getWidth()/2f,getHeight()/2f,p); p.setTextAlign(Paint.Align.LEFT); }
        for(int i=0;i<engine.garden.size();i++) {
            float x=dp(25)+(i*83)%(Math.max(1,(int)(getWidth()-dp(50))));
            float y=ground-((i*47)%Math.max(1,(int)(getHeight()*.32f)));
            float scale=.48f+(i%4)*.08f; drawFlower(c,engine.garden.get(i),x,y,scale,false,t+i*.3f);
        }
        drawInsects(c,t,ground);
    }

    private void drawFlower(Canvas c,FlowerGenome dna,float x,float ground,float scale,boolean chosen,float t) {
        float[] g=dna.genes; float stem=dp((68+g[0]*48)*scale), sway=(float)Math.sin(t*.7f+g[1]*6)*dp(3);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(Math.max(1.2f,2.5f*scale))); p.setColor(Color.rgb(70,(int)(125+g[2]*85),95));
        Path stemPath=new Path(); stemPath.moveTo(x,ground); stemPath.cubicTo(x-dp(8)*scale,ground-stem*.38f,x+sway,ground-stem*.72f,x+sway,ground-stem); c.drawPath(stemPath,p);
        drawLeaf(c,x-dp(2)*scale,ground-stem*.42f,-1,g[3],scale); drawLeaf(c,x+dp(2)*scale,ground-stem*.62f,1,g[4],scale);
        p.setStyle(Paint.Style.FILL); float cx=x+sway,cy=ground-stem;
        int petals=5+(int)(g[5]*8), layers=1+(int)(g[6]*3); float hue=g[7]*360, hue2=(hue+35+g[8]*145)%360;
        for(int layer=layers-1;layer>=0;layer--) {
            float radius=dp((26+g[9]*24-layer*5)*scale), width=dp((11+g[10]*16)*scale);
            for(int i=0;i<petals;i++) {
                float a=(float)(Math.PI*2*i/petals+layer*.19+g[11]*.28); c.save(); c.rotate((float)Math.toDegrees(a)+90,cx,cy);
                int col=Color.HSVToColor((int)(145+g[12]*90),new float[]{layer%2==0?hue:hue2,.38f+g[13]*.55f,.72f+g[14]*.28f});
                p.setColor(col); RectF petal=new RectF(cx-width/2,cy-radius,cx+width/2,cy+dp(4)*scale); c.drawOval(petal,p); c.restore();
            }
        }
        float core=dp((6+g[15]*8)*scale); p.setShader(new RadialGradient(cx,cy,core*2,Color.WHITE,Color.HSVToColor(new float[]{hue2,.8f,1}),Shader.TileMode.CLAMP)); c.drawCircle(cx,cy,core*2,p); p.setShader(null);
        p.setColor(chosen?Color.argb(70,255,240,255):Color.argb(35,220,190,255)); c.drawCircle(cx,cy,dp((34+g[16]*18)*scale),p);
    }

    private void drawLeaf(Canvas c,float x,float y,int side,float gene,float scale) {
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(53,(int)(115+gene*95),91)); Path leaf=new Path(); leaf.moveTo(x,y); leaf.quadTo(x+side*dp(24)*scale,y-dp(20)*scale,x+side*dp(40)*scale,y-dp(4)*scale); leaf.quadTo(x+side*dp(18)*scale,y+dp(8)*scale,x,y); c.drawPath(leaf,p);
    }

    private void drawInsects(Canvas c,float t,float ground) {
        for(int i=0;i<4;i++) { float x=(t*(24+i*5)+i*137)%getWidth(), y=ground-dp(80+i*38)-(float)Math.sin(t*1.7+i)*dp(28); p.setColor(Color.argb(180,255,235,155)); c.drawOval(x-dp(5),y-dp(2),x,y+dp(3),p); c.drawOval(x,y-dp(2),x+dp(5),y+dp(3),p); p.setColor(Color.rgb(70,52,66)); c.drawCircle(x,y+dp(2),dp(2.2f),p); }
    }

    private void drawButtons(Canvas c) {
        float y=getHeight()-dp(64),gap=dp(8),margin=dp(14),w=(getWidth()-margin*2-gap*2)/3;
        evolve.set(margin,y,margin+w,y+dp(48)); switchMode.set(margin+w+gap,y,margin+w*2+gap,y+dp(48)); reset.set(margin+w*2+gap*2,y,getWidth()-margin,y+dp(48));
        button(c,evolve,gardenMode?"Terug naar kweek":engine.selectedCount()==0?"Kies bloemen":"Nieuwe generatie",true);
        button(c,switchMode,gardenMode?"Kweektafel":"Mijn tuin",false); button(c,reset,"Opnieuw",false);
    }

    private void button(Canvas c,RectF r,String label,boolean bright) { p.setColor(bright?Color.rgb(123,82,149):Color.rgb(46,57,83)); c.drawRoundRect(r,dp(14),dp(14),p); p.setColor(Color.WHITE); p.setTextSize(dp(12)); p.setTextAlign(Paint.Align.CENTER); c.drawText(label,r.centerX(),r.centerY()+dp(4),p); p.setTextAlign(Paint.Align.LEFT); }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction()!=MotionEvent.ACTION_UP)return true; float x=e.getX(),y=e.getY();
        if(evolve.contains(x,y)) { if(gardenMode)gardenMode=false; else if(engine.selectedCount()>0)engine.evolve(); }
        else if(switchMode.contains(x,y)) gardenMode=!gardenMode;
        else if(reset.contains(x,y)) engine.reset();
        else if(!gardenMode) for(int i=0;i<cards.length;i++)if(cards[i].contains(x,y)){engine.toggle(i);break;}
        invalidate(); return true;
    }
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
}
