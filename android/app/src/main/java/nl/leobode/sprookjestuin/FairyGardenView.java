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
        float ground=getHeight()-dp(82);
        p.setColor(Color.rgb(31,48,61)); c.drawOval(-getWidth()*.2f,ground-dp(180),getWidth()*.75f,ground+dp(30),p);
        p.setColor(Color.rgb(24,67,51)); c.drawOval(getWidth()*.25f,ground-dp(145),getWidth()*1.2f,ground+dp(38),p);
        p.setColor(Color.rgb(18,74,48)); c.drawRect(0,ground-dp(42),getWidth(),ground,p);
        if(engine.garden.isEmpty()) { p.setColor(Color.rgb(225,210,236)); p.setTextSize(dp(17)); p.setTextAlign(Paint.Align.CENTER); c.drawText("Je tuin wacht op haar eerste gekozen bloemen",getWidth()/2f,getHeight()/2f,p); p.setTextAlign(Paint.Align.LEFT); }
        int beds=Math.min(6,engine.garden.size());
        for(int bed=0;bed<beds;bed++) {
            FlowerGenome variety=engine.garden.get(engine.garden.size()-beds+bed);
            int row=bed/3,col=bed%3; float depth=row==0?.72f:.18f;
            float centerX=(col+.5f)*getWidth()/3f+(row==0?dp(12):0);
            float base=ground-dp(8)-depth*dp(52);
            int count=4+(bed%3);
            // Each bed repeats one selected variety, as a real planted border would.
            for(int n=0;n<count;n++) {
                float spread=(n-(count-1)/2f)*dp(24);
                float stagger=((n*37+bed*19)%17-dp(0))*dp(.45f);
                float scale=(row==0?.54f:.76f)+(n%2)*.06f;
                drawFlower(c,variety,centerX+spread,base-stagger,scale,false,t+bed*.7f+n*.12f);
            }
        }
        drawInsects(c,t,ground);
    }

    private void drawFlower(Canvas c,FlowerGenome dna,float x,float ground,float scale,boolean chosen,float t) {
        float[] g=dna.genes; float stem=dp((62+g[0]*72)*scale), sway=(float)Math.sin(t*.7f+g[1]*6)*dp(3);
        int stemType=(int)(g[18]*4); float bend=dp((8+g[19]*25)*scale)*(g[20]>.5f?1:-1);
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(dp((1.2f+g[21]*3.2f)*scale)); p.setColor(Color.rgb((int)(40+g[22]*45),(int)(105+g[2]*105),(int)(58+g[22]*55)));
        Path stemPath=new Path(); stemPath.moveTo(x,ground);
        if(stemType==0) stemPath.cubicTo(x-bend*.15f,ground-stem*.35f,x+bend*.15f,ground-stem*.7f,x+sway,ground-stem);
        else if(stemType==1) stemPath.cubicTo(x+bend,ground-stem*.25f,x+bend,ground-stem*.76f,x+sway,ground-stem);
        else if(stemType==2) stemPath.cubicTo(x-bend,ground-stem*.28f,x+bend,ground-stem*.72f,x+sway,ground-stem);
        else { stemPath.lineTo(x+bend*.45f,ground-stem*.34f); stemPath.lineTo(x-bend*.25f,ground-stem*.68f); stemPath.lineTo(x+sway,ground-stem); }
        c.drawPath(stemPath,p); p.setStrokeCap(Paint.Cap.BUTT);
        int leaves=1+(int)(g[23]*4); int leafType=(int)(g[24]*3);
        for(int li=0;li<leaves;li++) { float frac=.28f+li*(.48f/Math.max(1,leaves-1)); int side=li%2==0?-1:1; drawLeaf(c,x+sway*frac,ground-stem*frac,side,g[(3+li)%18],scale,leafType); }
        p.setStyle(Paint.Style.FILL); float cx=x+sway,cy=ground-stem;
        int petals=5+(int)(g[5]*8), layers=1+(int)(g[6]*3); float hue=g[7]*360, hue2=(hue+35+g[8]*145)%360;
        float faceTilt=(g[25]-.5f)*1.25f;
        p.setColor(Color.argb(65,15,10,35));
        c.drawOval(cx-dp(31)*scale,cy-dp(21)*scale,cx+dp(35)*scale,cy+dp(29)*scale,p);
        for(int layer=layers-1;layer>=0;layer--) {
            float radius=dp((26+g[9]*24-layer*5)*scale), width=dp((11+g[10]*16)*scale);
            for(int i=0;i<petals;i++) {
                float a=(float)(Math.PI*2*i/petals+layer*.19+g[11]*.28);
                float perspective=.66f+.34f*(.5f+.5f*(float)Math.sin(a+faceTilt));
                int col=Color.HSVToColor((int)(145+g[12]*90),new float[]{layer%2==0?hue:hue2,.38f+g[13]*.55f,.72f+g[14]*.28f});
                drawPetal3D(c,cx,cy,width,radius,col,a,perspective,scale,layer);
            }
        }
        float core=dp((6+g[15]*8)*scale);
        p.setColor(Color.argb(100,20,12,35)); c.drawCircle(cx+dp(2.8f)*scale,cy+dp(4)*scale,core*2.18f,p);
        p.setShader(new RadialGradient(cx-core*.42f,cy-core*.48f,core*2.5f,Color.WHITE,Color.HSVToColor(new float[]{hue2,.8f,.62f}),Shader.TileMode.CLAMP)); c.drawCircle(cx,cy,core*2,p); p.setShader(null);
        p.setColor(Color.argb(115,255,245,190));
        for(int i=0;i<7;i++){float a=(float)(i*Math.PI*2/7);c.drawCircle(cx+(float)Math.cos(a)*core*1.15f,cy+(float)Math.sin(a)*core*1.15f,Math.max(dp(.7f),core*.11f),p);}
        p.setColor(chosen?Color.argb(70,255,240,255):Color.argb(35,220,190,255)); c.drawCircle(cx,cy,dp((34+g[16]*18)*scale),p);
    }

    private void drawPetal3D(Canvas c,float cx,float cy,float width,float length,int color,float angle,float perspective,float scale,int layer) {
        c.save(); c.rotate((float)Math.toDegrees(angle)+90,cx,cy); c.scale(1f,perspective,cx,cy);
        Path petal=new Path(); petal.moveTo(cx,cy+dp(3)*scale);
        petal.cubicTo(cx-width*.70f,cy-length*.22f,cx-width*.56f,cy-length*.83f,cx,cy-length);
        petal.cubicTo(cx+width*.56f,cy-length*.83f,cx+width*.70f,cy-length*.22f,cx,cy+dp(3)*scale); petal.close();
        c.save(); c.translate(dp(1.8f)*scale,dp(3.2f)*scale); p.setShader(null); p.setColor(darken(color,.36f)); c.drawPath(petal,p); c.restore();
        int highlight=lighten(color,.32f),shadow=darken(color,.27f);
        p.setShader(new LinearGradient(cx-width*.55f,cy-length,cx+width*.35f,cy,highlight,color,Shader.TileMode.CLAMP)); c.drawPath(petal,p); p.setShader(null);
        Path fold=new Path(); fold.moveTo(cx,cy+dp(1)*scale); fold.quadTo(cx-width*.18f,cy-length*.48f,cx,cy-length*.91f);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(dp(.6f),width*.055f)); p.setColor(Color.argb(95,Color.red(shadow),Color.green(shadow),Color.blue(shadow))); c.drawPath(fold,p); p.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private int lighten(int color,float amount) {
        int r=(int)(Color.red(color)+(255-Color.red(color))*amount),g=(int)(Color.green(color)+(255-Color.green(color))*amount),b=(int)(Color.blue(color)+(255-Color.blue(color))*amount);
        return Color.argb(Color.alpha(color),r,g,b);
    }

    private int darken(int color,float amount) {
        return Color.argb(Color.alpha(color),(int)(Color.red(color)*(1-amount)),(int)(Color.green(color)*(1-amount)),(int)(Color.blue(color)*(1-amount)));
    }

    private void drawLeaf(Canvas c,float x,float y,int side,float gene,float scale,int type) {
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb((int)(35+gene*45),(int)(110+gene*105),(int)(65+gene*55))); Path leaf=new Path(); leaf.moveTo(x,y);
        float length=dp((22+gene*27)*scale),height=dp((7+(1-gene)*15)*scale);
        if(type==0) { leaf.quadTo(x+side*length*.55f,y-height*1.4f,x+side*length,y); leaf.quadTo(x+side*length*.48f,y+height,x,y); }
        else if(type==1) { leaf.cubicTo(x+side*length*.18f,y-height,x+side*length*.82f,y-height*.35f,x+side*length,y); leaf.cubicTo(x+side*length*.72f,y+height*.55f,x+side*length*.22f,y+height,x,y); }
        else { leaf.cubicTo(x+side*length*.25f,y-height*1.4f,x+side*length*.62f,y-height,x+side*length,y); leaf.cubicTo(x+side*length*.58f,y+height*.2f,x+side*length*.2f,y+height*1.1f,x,y); }
        c.drawPath(leaf,p);
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
