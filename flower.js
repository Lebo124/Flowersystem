function Flower(dna_, x_, y_)  {
    this.rolloverOn = false; // Are we rolling over this flower?
    this.dna = dna_; // flower's DNA
    this.x = x_;     // Position on screen
    this.y = y_;
    this.wh = 200;      // Size of square enclosing flower
    this.fitness = 1; // How good is this flower?
    // Using java.awt.Rectangle (see: http://java.sun.com/j2se/1.4.2/docs/api/java/awt/Rectangle.html)
    this.r = new Rectangle(this.x-this.wh/2, this.y-this.wh/2, this.wh, this.wh);


  
  // makes a flower on end of branches
  this.set = function(){ 
  genes = this.dna.genes;
    
    c            = color(genes[0], genes[1], genes[2]);
    c1           = color(genes[3], genes[4], genes[5]);
    c2           = color(genes[6], genes[7],genes[8]);
    angle        = map(genes[9],0,1,40,80);
    d            = map(genes[10],0,1,150,400);
    s1           = map(genes[11],0,1,15,10);
    s2           = map(genes[12],0,1,-12,12);
    s3           = map(genes[13],0,1,-15,-6);
    s4           = map(genes[14],0,1,-7,-8);
    f = 1;
    }
  

  // make flower  with recursion
  
    this.display=function(){
  
       if (d>15) {
       f++;
       if ( d > 120){
        fill(c);
        }else{fill(c1);
        }
        push();
        strokeWeight(1);
        translate (this.x, this.y);
        rotate(angle*f);
        var e = map(d,150,400,12,18);
  
        bezier(0,0,50,50,50,50,20,20);
        bezier(0,0,s1*e,s2,s3*e,s4*e, 0, 0);
        bezier(0,0,s2*e,s3*e,s4,s1,0,0);
        fill(c2);
        ellipse(50,50,10,10);        
        
        pop();
        d =  d/2;
        this.display();
       }
       
  
    strokeWeight(0); // dont want to see the rectangle
    stroke("black");
    if (this.rolloverOn) fill(0, 0.25);
    else noFill();
    rectMode(CENTER);
    rect(this.x,this.y, this.wh, this.wh);
    pop();
    if (dr){this.dispFit();}
  }
    
    this.dispFit = function(){
    //strokeWeight(2);
    textAlign(CENTER);
    //console.log(this.fitness)
    //if (this.rolloverOn) fill(50);
    //else fill(0.25);
    strokeWeight(1); textSize(12); textStyle(NORMAL);
    text('' + floor(this.fitness), this.x, this.y+150);
    strokeWeight(2);
    }
  

    this.getFitness = function() {
    return this.fitness;
  }

  this.getDNA = function() {
    return this.dna;
  }

  // Increment fitness if mouse is rolling over flower
  this.rollover = function(mx, my) {
    if ((this.r).contains(mx, my)) {
      this.rolloverOn = true;
      this.fitness += 0.25;
    } else {
      this.rolloverOn = false;
    }
    if (this.fitness>50){ // maximum fitness = 50
      this.fitness=50
    }
  }
}



  
