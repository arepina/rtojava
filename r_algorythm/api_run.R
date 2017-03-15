require(plumber)

r <- plumb("api.R")
r$run(port=5469
      , host="0.0.0.0"
      )
