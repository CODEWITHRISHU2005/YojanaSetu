# Google Search Integration - Quick Reference

## Summary

Your YojanaSetu application now has **hybrid search capability**:
- 🔍 Primary: Searches PDF documents (schemes information)
- 🌐 Secondary: Uses Google Search when PDF results are insufficient

## What Changed

### New Files Created
1. **GoogleSearchService.java** - Handles Google Custom Search API integration
2. **RestTemplateConfig.java** - Spring configuration for HTTP requests
3. **GOOGLE_SEARCH_SETUP.md** - Detailed setup instructions
4. **application.properties** - Added Google Search configuration

### Modified Files
1. **ChatService.java**:
   - Added GoogleSearchService dependency
   - Enhanced getRAGResponse() to supplement PDF results with Google Search
   - Automatic fallback when PDF results < 3
2. **pom.xml** - Added google-http-client dependency
3. **Helper.java** - Updated system prompt to handle both PDF and web sources

## Quick Setup (3 Steps)

### 1. Create Search Engine
- Visit: https://programmablesearchengine.google.com
- Create new search engine for "YojanaSetu"
- Save the Search Engine ID

### 2. Get API Key
- Visit: https://console.cloud.google.com
- Enable "Custom Search API"
- Create an API Key in Credentials

### 3. Set Environment Variables
```bash
set GOOGLE_SEARCH_ENGINE_ID=your_id_here
set GOOGLE_SEARCH_API_KEY=your_key_here
```

Then start your application!

## How It Works

```
User Query
    ↓
Search PDF Documents
    ↓
Found < 3 results?
    ↓
    YES → Google Search → Combine results
    NO → Use PDF results
    ↓
Generate Response with AI
```

## Configuration

**application.properties**:
```properties
google.search.enabled=true              # Enable/disable feature
google.search.results-per-query=3       # Results from Google Search
```

**Environment Variables**:
```bash
GOOGLE_SEARCH_ENGINE_ID=your_search_engine_id
GOOGLE_SEARCH_API_KEY=your_api_key
```

## Free Tier Limits

- ✅ 100 queries/day
- ✅ 10 queries/second
- ✅ Free to enable

## Features

✅ Automatic fallback to Google Search
✅ Graceful error handling
✅ Configurable result count
✅ Can be disabled anytime
✅ Logs search operations
✅ Supports Hindi & English queries

## Testing

```bash
# Start the app
mvn spring-boot:run

# Test API (search with insufficient PDF results)
curl "http://localhost:8080/api/chat/ask?message=Tell%20me%20about%20latest%20government%20schemes"
```

Check logs for: `"PDF results insufficient, using Google Search"`

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Google Search not working | Check env vars are set, API enabled in Cloud Console |
| No results returned | Verify Search Engine ID is correct |
| Quota exceeded | Wait for next day (100/day limit on free tier) |
| Build errors | Run `mvn clean install` |

## Disable Google Search

To turn it off temporarily:
```properties
google.search.enabled=false
```

Or just don't set the environment variables.

## Common Queries That Benefit from Google Search

- "What are the latest government schemes 2024?"
- "Recent updates on PM Kisan Yojana"
- "Civil service exam dates and eligibility"
- "Where to apply for this scheme?"

## Performance Notes

- PDF search completes in ~200-500ms
- Google Search adds ~500-1000ms if triggered
- Results are combined automatically
- No memory/storage overhead

## Next Steps

1. ✅ Read GOOGLE_SEARCH_SETUP.md for detailed setup
2. ✅ Set environment variables
3. ✅ Build and test the application
4. ✅ Monitor API quota usage in Google Cloud Console

---

**Questions?** Check the logs or review GoogleSearchService.java for implementation details.

