

function Branch(start, vel, n) {
  this.start = start.copy();
  this.end = start.copy();
  this.vel = vel.copy();
  this.timerstart = n;
  this.timer = n;
  this.growing = true;
  this.angle = random (30,70);
  
  this.update = function() {
    if (this.growing) {
      this.end.add(this.vel);

    }
  }

  this.render = function() {
    colorMode(RGB);
    strokeWeight(2);
    stroke("lightgreen");
    fill("green");
    quad(this.end.x, this.end.y, this. end.x+15, this.end.y, this.start.x, 
         this.start.y, this.start.x+15, this.start.y+10);
    stroke("black");

   }
   // when timer = 0 new branches
  this.timeToBranch = function() {
    this.timer--;
    if (this.timer < 0 && this.growing) {
      this.growing = false;
      return true;
    } else {
      return false;
    }
  }

  this.branch = function(angle) {
    // What is my current heading
    var theta = vel.heading();
    // What is my current speed
    var m = vel.mag();
    // Turn me
    theta += radians(angle);
    // Look, polar coordinates to cartesian!!
    var newvel = createVector(m * cos(theta), m * sin(theta));
    // Return a new Branch
    //if(this.timerstart < 4){ this.timerstart=8};
    return new Branch(this.end, newvel, 50);

  }

}
