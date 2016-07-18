// Leo Bode
// Kadenze : Code of Nature
// by Daniel Shiffman
// based on code by Daniel Shiffman.

// In this sketch you can give six different flowers 
// a fitness rate (max=50).
//After that by pressing "Show matepool" a tree is build
// with all the flowers in the matepool.
// Then you can switch to the next generation.
var matPool = [];
var tree = [];
var planet; // background tree (does not work with Chrome)
var population;
var info;
//variabels for the genes
    var genes;
    var b;
    var c;
    var c1;
    var angle;
    var ss;
    var d;
    var f=1; 
    var s1, s2, s3, s4;   
    var dr=true; // Switch between matPool and new Generation

function setup() {
  createCanvas(1200,600);
  colorMode(RGB,1.0, 1.0, 1.0, 1.0);
  strokeWeight(2);
  planet = loadImage("assets/planet.jpg");
  
  var popmax = 6; // six different flowers
  var mutationRate = 0.15;  // A pretty high mutation rate here, our population is rather small we need to enforce variety
  // Create a population with a target phrase, mutation rate, and population max
  population = new Population(mutationRate,popmax);
  

  // two buttons for switching between show matepool and next generation
  button = createButton("show matingpool");
  button1 = createButton("next generation");
  button.mousePressed(nextTree);
  button1.mousePressed(nextGen);
  button.position(10,400);
  button1.position(10,500);
  info = createDiv('');
  info.position(10,450);
  info1 = createDiv("");
  info.style("font-family", "arial")
  info1.style("font-family", "arial")
  info1.position(200,450);
}

function draw(){
  if(dr){

  background(255);
  button.show();
  button1.hide();
  population.display();
  population.rollover(mouseX,mouseY);
  info.html("Generation #:" + population.getGenerations());
  info1.style("color", "blue");
  info1.html('You can rate the flowers you like and give them a number<br> until 50 by putting the mouse on the flower.<br>After that push the "show matingpool" button.');
 }else{
   background(150);
   background(planet); //Does not work in Chrome!!
   button1.show();
   button.hide();
   info.html("");
   info1.style("color", "white");
   info1.html('If you are ready watching. Press next generation.<br> The program will make a new generation out of the chosen flowers. <br> It will mutate some genes. ');
   makeTree();
 }
}


// If the button is clicked, evolve new tree
  function nextTree() {
  matPool = population.selection();
  tree = [];
  var b = new Branch(createVector(width / 2, height), createVector(0, -3), 80);
  tree.push(b);
  
  dr=false;
};
// If the button1 is clicked, evolve new generation
  function nextGen(){
  population.reproduction();
  dr=true; // 
  loop();
};

// Build the new tree. The flowers are big, so i gave the branches and the angles more value
// compared to assignement 4
function makeTree(){
   console.log(tree);
   for (var i = 0; i < tree.length; i++) {
    // Get the branch, update and draw it
    tree[i].update();
    tree[i].render();
    

    if (tree[i].timeToBranch()) {

      if (tree.length < matPool.length+3) {
       
          angle = (floor(random(70, 50)));
          tree.push(tree[i].branch(angle)); // Add one going right, angle randomly between 50,30
          angle = (floor(random(-70, -50)));
          tree.push(tree[i].branch(angle));// add one going left, angle randomly
          angle = (floor(random(20, 40)));
          tree.push(tree[i].branch(angle)); // Add one going right, angle randomly between 50,30
          angle = (floor(random(-20, -40)));
          tree.push(tree[i].branch(angle)); // Add one going right, angle randomly between 50,30
          
      } else {

         for (var i = 0; i < matPool.length; i++) {
         //draw the flowers in the matepool
          var gen = matPool[i].getDNA();
          var x=tree[i].end.x;
          var y=tree[i].end.y;
          var f = new Flower(gen,x,y);
          f.set();
          f.display();
           //console.log(i + " " + x +" "+ y);
  
      }
      //if the tree is ready stop loop until button1 is pressed
      noLoop();
    }
  }
 }
}