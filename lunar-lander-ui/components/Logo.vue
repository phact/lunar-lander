<template>
  <div class="frame">
    <div class="lander-body" >
    </div>
    <div class="legs" >
        <div class="leg l1" >
        </div>
        <div class="leg l2" >
        </div>
        <div class="leg l3" >
        </div>
    </div>
    <div class="feet" >
          <div class="lander-foot f1" />
          <div class="lander-foot f2" />
          <div class="lander-foot f3" />
    </div>
  </div>
</template>

<style>
.frame{
  height:18em;
  width: 30em;
  animation: turn 2s linear forwards 1s;
  transform: rotateX(360deg);
  font-size:10px;
}

.lander-body{
  width: 100%;
  position: relative;
  overflow: hidden;
  background-image: linear-gradient(to bottom, #211c17, #080707);
  background-size: 100% 100%;
  border-left: 5em solid transparent;
  border-right: 5em solid transparent;
  border-bottom: 10em solid gray;
  #border-radius: 90% 50% 90% 50% / 90% 50% 90% 50%;
  #box-shadow: inset 0.9em 0.9em 0.4em -0.8em #150c0bb5;
  #filter: blur(0.2em);
  height:0
}
  
.legs{
  width: 100%;
  display: inline-block;
  position: relative;
  top: 0;
  left: 0;
  height: 0;
}

.leg{
  display: inline-block;
  height:0;
  width: 7em;
}

.leg.l1{
  animation: goleft 0.5s linear forwards 3.5s;
  border-bottom: 6em solid gray;
  border-left: 3em solid transparent;
  border-right: 1.5em solid transparent;
}

.leg.l2{
  border-left: 2em solid transparent;
  border-right: 2em solid transparent;
  border-bottom: 6em solid gray;
  margin-left: 2vw;
  margin-right: 2vw;
}

.leg.l3{
  animation: goright 0.5s linear forwards 3.5s;
  border-left: 1.5em solid transparent;
  border-right: 3em solid transparent;
  border-bottom: 6em solid gray;
}

.feet{
  width: 100%;
  display: inline-block;
  position: relative;
  top: 0;
  left: 0;
  height: 0;
}


.lander-foot{
  display: inline-block;
  height:0;
  width: 7em;
}

.lander-foot.f1{
  animation: goleft 0.5s linear forwards 3.5s; 
  border-bottom: 2em solid black;
  border-left: 1em solid transparent;
  border-right: .5em solid transparent;
}

.lander-foot.f2{
  border-bottom: 2em solid black;
  border-left: .5em solid transparent;
  border-right: .5em solid transparent;
  margin-left: 2vw;
  margin-right: 2vw;
}

.lander-foot.f3{
  animation: goright 0.5s linear forwards 3.5s;
  border-bottom: 2em solid black;
  border-left: .5em solid transparent;
  border-right: 1em solid transparent;
}



@keyframes turn {
  100% {
    transform: rotateX(0deg);
  }
}

@keyframes godown {
  100% {
    top: 18em;
  }
}

@keyframes goright {
  100% {
    left: 7em;
  }
}
</style>
