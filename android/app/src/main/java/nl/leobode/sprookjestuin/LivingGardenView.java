package nl.leobode.sprookjestuin;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.Locale;

/** Interactive accelerated garden: genes -> regulatory substances -> development -> fitness. */
public final class LivingGardenView extends View {
    private final DevelopmentEngine engine=new DevelopmentEngine();
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF sunButton=new RectF(),rainButton=new RectF(),shadeButton=new RectF(),pauseButton=new RectF();
    private long lastStep=SystemClock.uptimeMillis(); private int selected=-1;
    public LivingGardenView(Context c){super(c);}

    @Override protected void onDraw(Canvas c){
        long now=SystemClock.uptimeMillis();
        if(now-lastStep>520){engine.tickDay();lastStep=now;}
        drawSky(c); drawGarden(c); drawHeader(c); drawPanel(c); drawControls(c); postInvalidateOnAnimation();
    }
    private void drawSky(Canvas c){
        int top=Color.rgb(13,24,51), bottom=Color.rgb(56,65,86);
        p.setShader(new LinearGradient(0,0,0,getHeight(),top,bottom,Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);
        p.setColor(Color.argb(95,220,205,255));for(int i=0;i<20;i++)c.drawCircle((i*197)%getWidth(),dp(34)+(i*83)%Math.max(1,(int)(getHeight()*.43f)),dp(i%4==0?1.7f:.8f),p);
        float sx=getWidth()-dp(48),sy=dp(55);p.setColor(Color.argb((int)(80+engine.sun*120),255,226,125));c.drawCircle(sx,sy,dp(19),p);
        if(engine.rain>.7f){p.setStrokeWidth(dp(1.4f));p.setColor(Color.argb(120,145,205,255));for(int i=0;i<24;i++){float x=(i*73)%getWidth();c.drawLine(x,dp(82)+(i*31)%dp(210),x-dp(4),dp(94)+(i*31)%dp(210),p);}}
    }
    private void drawHeader(Canvas c){
        p.setColor(Color.WHITE);p.setTextSize(dp(23));p.setFakeBoldText(true);c.drawText("Levende Sprookjestuin",dp(18),dp(34),p);p.setFakeBoldText(false);
        p.setColor(Color.rgb(222,207,235));p.setTextSize(dp(12));c.drawText(String.format(Locale.getDefault(),"Generatie %d  •  dag %d van %d",engine.generation,engine.day,DevelopmentEngine.SEASON_DAYS),dp(18),dp(55),p);
    }
    private void drawGarden(Canvas c){
        float bottom=getHeight()-dp(selected>=0?164:112),top=dp(76);p.setColor(Color.rgb(22,65,49));c.drawOval(-dp(80),top+dp(80),getWidth()+dp(120),bottom+dp(55),p);
        Path path=new Path();path.moveTo(getWidth()*.43f,bottom);path.cubicTo(getWidth()*.52f,bottom-dp(75),getWidth()*.47f,top+dp(120),getWidth()*.55f,top);path.lineTo(getWidth()*.64f,top);path.cubicTo(getWidth()*.56f,top+dp(120),getWidth()*.67f,bottom-dp(70),getWidth()*.65f,bottom);path.close();p.setColor(Color.rgb(112,96,82));c.drawPath(path,p);
        for(int i=0;i<engine.plants.size();i++){int row=i/4,col=i%4;float side=col<2?-.25f:.25f;float lane=(col%2)*.12f;float x=getWidth()*(.5f+side+(side<0?-lane:lane));float y=bottom-row*dp(78)-((col%2)*dp(16));float scale=.80f-row*.11f;drawPlant(c,engine.plants.get(i),x,y,scale,i==selected);}
    }
    private void drawPlant(Canvas c,DevelopmentEngine.Plant q,float x,float ground,float scale,boolean marked){
        float h=dp((18+q.height*105)*scale),bend=dp(q.stemBend()*22*scale),cx=x+bend,cy=ground-h;
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(dp((1.8f+q.health*2)*scale));p.setColor(Color.rgb(44,(int)(105+q.health*90),70));Path stem=new Path();stem.moveTo(x,ground);stem.cubicTo(x-bend*.3f,ground-h*.4f,x+bend*.5f,ground-h*.72f,cx,cy);c.drawPath(stem,p);p.setStyle(Paint.Style.FILL);
        int leaves=Math.min(7,q.leaves());for(int i=0;i<leaves;i++){float f=.28f+i*.5f/Math.max(1,leaves-1),lx=x+bend*f,ly=ground-h*f,s=i%2==0?-1:1;Path leaf=new Path();leaf.moveTo(lx,ly);leaf.quadTo(lx+s*dp(15)*scale,ly-dp(9)*scale,lx+s*dp(26)*scale,ly);leaf.quadTo(lx+s*dp(12)*scale,ly+dp(8)*scale,lx,ly);p.setColor(Color.rgb(38,(int)(115+q.health*80),77));c.drawPath(leaf,p);}
        if(q.stage()==0){p.setColor(Color.rgb(105,80,62));c.drawOval(x-dp(4),ground-dp(3),x+dp(4),ground+dp(3),p);return;}
        if(q.bud>.08f&&q.flower<.12f){p.setColor(Color.rgb(108,73,143));c.drawOval(cx-dp(5)*scale,cy-dp(8)*scale,cx+dp(5)*scale,cy+dp(5)*scale,p);}
        if(q.flower>.10f){float size=dp((8+q.flower*19)*scale),hue=q.hue()*360;int petals=q.petals();p.setColor(Color.argb((int)(25+q.glow()*55),220,160,255));c.drawCircle(cx,cy,size*1.35f,p);for(int i=0;i<petals;i++){float a=(float)(i*Math.PI*2/petals),px=cx+(float)Math.cos(a)*size*.75f,py=cy+(float)Math.sin(a)*size*.58f;float rw=size*(.34f+q.petalRoundness()*.24f),rh=size*(.18f+q.petalRoundness()*.25f);c.save();c.rotate((float)Math.toDegrees(a),px,py);p.setColor(Color.HSVToColor(new float[]{hue,.46f+q.pigmentB*.38f,.72f+q.glow()*.25f}));c.drawOval(px-rw,py-rh,px+rw,py+rh,p);c.restore();}p.setShader(new RadialGradient(cx-size*.2f,cy-size*.25f,size*.55f,Color.WHITE,Color.HSVToColor(new float[]{(hue+55)%360,.72f,.75f}),Shader.TileMode.CLAMP));c.drawCircle(cx,cy,size*.42f,p);p.setShader(null);}
        if(marked){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(Color.WHITE);c.drawCircle(cx,cy,dp(28)*scale,p);p.setStyle(Paint.Style.FILL);}
    }
    private void drawPanel(Canvas c){if(selected<0)return;DevelopmentEngine.Plant q=engine.plants.get(selected);float y=getHeight()-dp(154);p.setColor(Color.argb(220,26,35,57));c.drawRoundRect(dp(12),y,getWidth()-dp(12),getHeight()-dp(76),dp(14),dp(14),p);p.setColor(Color.WHITE);p.setTextSize(dp(12));c.drawText("Binnenin plant "+(selected+1)+"  •  "+stage(q.stage()),dp(24),y+dp(21),p);meter(c,"groei",q.growthSignal,dp(24),y+dp(39),Color.rgb(90,210,130));meter(c,"rem",q.growthBrake,getWidth()*.36f,y+dp(39),Color.rgb(240,155,105));meter(c,"bloei",q.bloomSignal,getWidth()*.68f,y+dp(39),Color.rgb(211,110,235));meter(c,"pigment A",q.pigmentA,dp(24),y+dp(61),Color.rgb(104,154,255));meter(c,"pigment B",q.pigmentB,getWidth()*.36f,y+dp(61),Color.rgb(235,92,174));meter(c,"stress",q.stress,getWidth()*.68f,y+dp(61),Color.rgb(247,102,90));}
    private void meter(Canvas c,String label,float v,float x,float y,int color){float w=getWidth()*.22f;p.setTextSize(dp(9));p.setColor(Color.rgb(220,215,229));c.drawText(label,x,y,p);p.setColor(Color.argb(80,255,255,255));c.drawRoundRect(x,y+dp(4),x+w,y+dp(9),dp(3),dp(3),p);p.setColor(color);c.drawRoundRect(x,y+dp(4),x+w*v,y+dp(9),dp(3),dp(3),p);}
    private String stage(int s){return s==0?"zaad":s==1?"groei":s==2?"knop":s==3?"bloei":"zaadzetting";}
    private void drawControls(Canvas c){float y=getHeight()-dp(62),m=dp(10),g=dp(6),w=(getWidth()-m*2-g*3)/4;sunButton.set(m,y,m+w,y+dp(48));rainButton.set(m+w+g,y,m+w*2+g,y+dp(48));shadeButton.set(m+w*2+g*2,y,m+w*3+g*2,y+dp(48));pauseButton.set(m+w*3+g*3,y,getWidth()-m,y+dp(48));button(c,sunButton,"☀  Zon",engine.sun>.7f);button(c,rainButton,"☂  Regen",engine.rain>.7f);button(c,shadeButton,"☁  Schaduw",engine.shade>.7f);button(c,pauseButton,engine.paused?"Verder":"Pauze",engine.paused);}
    private void button(Canvas c,RectF r,String text,boolean active){p.setColor(active?Color.rgb(128,79,154):Color.rgb(44,57,84));c.drawRoundRect(r,dp(12),dp(12),p);p.setColor(Color.WHITE);p.setTextSize(dp(11));p.setTextAlign(Paint.Align.CENTER);c.drawText(text,r.centerX(),r.centerY()+dp(4),p);p.setTextAlign(Paint.Align.LEFT);}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();if(sunButton.contains(x,y))engine.setWeather(0);else if(rainButton.contains(x,y))engine.setWeather(1);else if(shadeButton.contains(x,y))engine.setWeather(2);else if(pauseButton.contains(x,y))engine.paused=!engine.paused;else{float bottom=getHeight()-dp(selected>=0?164:112);int best=-1;float dist=dp(45);for(int i=0;i<engine.plants.size();i++){int row=i/4,col=i%4;float side=col<2?-.25f:.25f,lane=(col%2)*.12f,px=getWidth()*(.5f+side+(side<0?-lane:lane)),py=bottom-row*dp(78)-((col%2)*dp(16));float d=(float)Math.hypot(x-px,y-py);if(d<dist){dist=d;best=i;}}selected=best;}invalidate();return true;}
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
}
