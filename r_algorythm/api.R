require(magrittr)
options(warn=-1)

Sys.setlocale(locale = "Russian")
options(encoding = "utf-8")

c(
  "tm",
  "data.table",
  "dplyr",
  "dtplyr",
  "slam",
  "quanteda",
  "Matrix",
  "qlcMatrix",
  "ggplot2",
  "stringr",
  "stringi",
  "fastmatch",
  "proxy",
  "lubridate"
) %>%
  sapply(require, character.only = TRUE)
pat <- readRDS("../pat")
pat_terms <- readRDS("../pat_terms")

#' @assets ./files/static
dfm <- readRDS("dfm")
corp <- readRDS("products")

Sys.setlocale(locale = "russian")

get_log_dummy <- function(){
  entry <- sapply(c(
    "start_time",
    "name",
    "regions",
    "measure",
    "measure_stats",
    "okdp",
    "rev_okpd",
    "first_noun",
    "noun_found",
    "clean_name",
    "prepare_query",
    "first_intersect",
    "nrow_first_intersect",
    "second_intersect",
    "nrow_second_intersect",
    "threshold",
    "filtered_by_threshold",
    "cosine",
    "join",
    "filtered_by_measure",
    "filtered_by_region",
    "filtered_by_okdp",
    "filtered_by_price",
    "price_min",
    "price_rec",
    "price_max",
    "process_time",
    "end_time"
  ),
  function(x)
    NA)
  entry$is_unknown <- FALSE
  entry$all_russia <- TRUE
  entry$measure_set <- TRUE
  entry
}

write_log <- function(entry){
  print(unlist(entry))
  write.table(unlist(entry) %>% t, 
              paste0("~\\apps\\NMZK_api\\logs\\", Sys.Date()), 
              append = TRUE,
              row.names = FALSE,
              col.names = FALSE,
              fileEncoding = "CP1251")
}

get_result_dummy <- function(){
  data.frame(date = numeric(),
             okdp = numeric(),
             name = numeric(),
             measure = numeric(),
             price = numeric(),
             region = numeric(),
             href = numeric(),
             rank = numeric())
}

`%fin%` <- function(x, table) {
  stopifnot(require(fastmatch))
  fmatch(x, table, nomatch = 0L) > 0L
}

get_cosine <- function(tfidf_m, q) {
  cosine <- qlcMatrix::cosSparse(quanteda::t(tfidf_m), quanteda::t(q))
  cosine
}

find_similar_cosine <- function(mat, query_row) {
  matr <- get_cosine(mat, query_row)
  data.table(product_id = matr@Dimnames[[1]],
             cos = matr[, "text1"])
}

okdp_checker <- function(okdp_q, okdp_s) {
  res <- (okdp_q - okdp_s) %>% as.character
  trimmed <- stri_trim_right(res, "\\P{numeric_value=0}")
  match <- ifelse(res == 0, 9, nchar(res) - nchar(trimmed))
}

trim <- function (x)
  gsub("^\\s+|\\s+$", "", x)

append_terms <- function(query) {
  
  query$noun_found <- TRUE
  
  gr_words <- query$name %>% 
    toLower %>% 
    gsub(pattern = "\"", replacement = "", fixed = TRUE, x = .) %>% 
    gsub(pattern = "(\\d)(\\D)", replacement = "\\1\\ \\2", x = .) %>%
    gsub(pattern = "(\\D)(\\d)", replacement = "\\1\\ \\2", x = .) %>%
    gsub(pattern = "(\\D)(\\.|,)(\\D)", replacement = "\\1\\ \\3", x = .) %>%
    removePunctuation %>%
    quanteda::tokenize() %>%
    .[[1]] %>% 
    system("mystem -cgi --eng-gr", intern = TRUE, input = .) %>%
    iconv(from = "UTF-8", to = "WINDOWS-1251") %>% enc2utf8
  
  pts <- str_match(gr_words, pat_terms)
  
  if(any(pts[,5] == "S", na.rm = TRUE)){
    first_noun = pts[,3][which(pts[,5] == "S") %>% min]
  } else {
    query$noun_found <- FALSE
    first_noun = pts[,3][which(!is.na(pts[,3])) %>% min]
  }
  numbers <- gr_words %>% as.integer %>% .[!is.na(.)]
  
  query$first_noun <- first_noun
  query$clean_name <- paste(c(pts[,3][!is.na(pts[,3])], numbers), collapse = " ")
  query
}

prepare_query <- function(query) {
  if(is.null(query$regions)) query$regions <- ""
  if(is.null(query$okdp)) query$okdp <- ""
  query$measure  %<>% tolower
  query$rev_okpd <- query$okdp %>%  gsub("[.]", "", .) %>% stri_reverse %>% as.integer
  query %<>% append_terms
  query
}

#' @get /mean
normalMean <- function(samples=10){
  data <- rnorm(samples)
  mean(data)
}


#' @get /get_closest
get_closest <- function(name = "Mercedes", regions = "", measure = "", okdp = ""){
  name %<>% iconv(from = "utf-8", to = "WINDOWS-1251") %>% substr(2, nchar(.)-1)
  measure %<>% iconv(from = "utf-8", to = "WINDOWS-1251") %>% substr(2, nchar(.)-1)
  okdp %<>% substr(2, nchar(.)-1)
  regions %<>% substr(2, nchar(.)-1)
  regions %<>% stri_split_fixed(",") %>% unlist
  okdp %<>% stri_split_fixed(",") %>% unlist
  query <- list(name = name, regions = regions, measure = measure, okdp = okdp)
  print(query)
  get_table(query, corp, dfm)
}


get_table <- function(query, corp, dfm) {
  entry <- get_log_dummy()
  entry$start_time <- Sys.time()
  
  if(query$name == ""){
    return(jsonlite::toJSON(get_result_dummy()))
  }
  
  query %<>% prepare_query
  
  print(query)
  
  entry$prepare_query <- Sys.time() - entry$start_time
  
  entry$name <- query$name
  entry$regions <- query$regions %>% paste(collapse = " ")
  entry$okdp <- query$okdp
  entry$rev_okpd <- query$rev_okpd
  entry$noun_found <- query$noun_found
  entry$first_noun <- query$first_noun
  entry$clean_name <- query$clean_name
  
  if(!query$first_noun %in% dfm@Dimnames$features){
    entry$is_unknown = TRUE
    write_log(entry)
    return(jsonlite::toJSON(get_result_dummy()))
  }
  
  query_tf <-
    query$clean_name %>% quanteda::tokenize() %>%
    dfm(language = "russian", verbose = FALSE)
  query_tf@x <- rep(1, query_tf@x %>% length)
  
  t <- Sys.time()

  match_tf <- dfm[Matrix::rowSums(dfm[, query$first_noun]) == 1, ]
  threshold <- 0.7

  entry$first_intersect <- Sys.time() - t
  entry$nrow_first_intersect <- nrow(match_tf)
  
  t <- Sys.time()

  if(length(features(query_tf)) == 1){
    threshold <- 0.5
  }
  

  if(length(features(query_tf)) > 1 & all(features(query_tf) %fin% features(match_tf))){
    new_tf <- match_tf[Matrix::rowSums(match_tf[, features(query_tf)]) == length(features(query_tf)), ]
    entry$nrow_second_intersect <- nrow(new_tf)
    if(nrow(new_tf) > 0){
      threshold <- 0.5
      match_tf <- new_tf
    }
  }

  entry$second_intersect <- Sys.time() - t
  entry$threshold <- threshold

  t <- Sys.time()
  
  match_tf  <- match_tf[, Matrix::colSums(match_tf) > 0]

  mat <- rbind(match_tf, query_tf)
  ind <-
    find_similar_cosine(mat[-nrow(mat),], mat[nrow(mat),]) 
  setorder(ind, -cos)
  ind <- ind[ind$cos >= threshold,]
  ind %>% setkey("product_id")
  
  entry$filtered_by_threshold <- nrow(ind)
  entry$cosine <- Sys.time() - t
  t <- Sys.time()
  
  result <-
    left_join(corp[corp$product_id %fin% ind$product_id,],
              ind, by = "product_id") %>% as.data.frame
  
  entry$join <- Sys.time() - t

  t <- Sys.time()
  stats <- result %>% group_by(product_measure) %>% summarise(
    count = n()) %>% arrange(desc(count)) %>% 
    filter(!is.na(product_measure)) 

  if(nchar(query$measure) == 0){
    entry$measure_set <- FALSE
    query$measure <- stats$product_measure[1]
  }

  entry$measure_stats <- Sys.time() - t
  
  entry$measure <- query$measure
  

  measures <- stats$product_measure
  if (any(measures == query$measure)) {
    measures <- measures[-which(measures == query$measure)]
  }
  result$product_measure <- ordered(result$product_measure,
                                    levels = c(query$measure, measures))

  result %<>% filter(product_measure == query$measure)
  result$product_price %<>% as.numeric
  
  entry$filtered_by_measure <- nrow(result)
  print(query)
  if(nrow(result %>% filter(region_code %fin% query$regions)) > 20) {
    entry$all_russia <- FALSE
    result %<>% filter(region_code %fin% query$regions)
  }

  entry$filtered_by_region <- nrow(result)
  
  if(!is.na(query$rev_okpd)){
    result$match_okdp <- sapply(query$rev_okpd, okdp_checker, okdp_s = result$rev_okpd) %>% 
      rowMax %>% 
      as.vector
  } else {
    result$match_okdp <- 9
  }
  result %<>% filter(match_okdp > 1) 
  entry$filtered_by_okdp <- nrow(result)
  
  result %<>%
    mutate(
      total_score = 0.7 * cos + 0.3 * match_okdp / 9,
      total_score_rank = ifelse(total_score >= 0.8,
                                1,
                                total_score)
    )
  
  result$month <- month(result$ct_publish_date) %>% as.factor

  result %<>% group_by(month) %>%
    filter(product_price >= quantile(product_price, 0.3) &
             product_price <= quantile(product_price, 0.7)) %>%
    ungroup
  result %<>% select(-month)
  
  # result %<>% filter(product_price >= quantile(product_price, 0.3) &
  #                      product_price <= quantile(product_price, 0.7))
  entry$filtered_by_price <- nrow(result)
  
  result <- arrange(result,
                    desc(total_score_rank)
                    ,
                    desc(match_okdp))
  result %<>%
    select(c(
      date = ct_publish_date,
      okpd2 = product_okpd_2,
      name = product_name,
      measure = product_measure,
      price = product_price,
      region = region_code,
      link = ct_href,
      rank = total_score_rank
    )) 
  entry$price_min <- result$price %>% min
  entry$price_max <- result$price %>% max
  entry$price_rec <- result$price %>% mean
  
  entry$process_time <- Sys.time() - entry$start_time
  entry$end_time <- Sys.time()
  write_log(entry)
  print(nrow(result))
  jsonlite::toJSON(result, dataframe = "columns")
}
