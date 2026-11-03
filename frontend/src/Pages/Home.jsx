import { useEffect } from "react";

function Home() {   
  useEffect(() => {
    document.title = "Welcome to GPF Application";
  }, []);

  // Fix: You must return JSX for the component to mount and run the useEffect
  //return (
    //<div>
      //<h1>Welcome to the GPF Application</h1>
    //</div>
  //);
}

export default Home;
