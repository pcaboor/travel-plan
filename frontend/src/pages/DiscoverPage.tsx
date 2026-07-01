import { useState } from "react";
import { Loader2, Search, Sparkles, Ticket } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  useAutocomplete,
  useRecommendations,
  useSubscribe,
  useTravelSearch,
} from "@/features/discovery/api";

type CardData = { id: string; title: string | null; subtitle?: string; badge?: string };

export function DiscoverPage() {
  const [query, setQuery] = useState("");
  const [committed, setCommitted] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  const autocomplete = useAutocomplete(query);
  const results = useTravelSearch(committed);
  const recommendations = useRecommendations();
  const subscribe = useSubscribe();

  const runSearch = (q: string) => {
    setMessage(null);
    setCommitted(q.trim());
  };

  const onSubscribe = async (id: string, title: string | null) => {
    setMessage(null);
    try {
      const res = await subscribe.mutateAsync(id);
      setMessage(`Subscribed to “${title ?? id}” — booking ${res.status}. Complete the payment to confirm.`);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Subscription failed");
    }
  };

  const showSuggestions =
    query.trim().length >= 2 &&
    committed !== query.trim() &&
    (autocomplete.data?.length ?? 0) > 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Discover</h1>
        <p className="text-sm text-muted-foreground">
          Search travels, get personalized suggestions, and subscribe.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Search</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="relative">
            <div className="flex gap-2">
              <Input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && runSearch(query)}
                placeholder="Search by title, destination or activity…"
              />
              <Button onClick={() => runSearch(query)} disabled={!query.trim()}>
                <Search className="mr-2 h-4 w-4" />
                Search
              </Button>
            </div>
            {showSuggestions && (
              <div className="absolute z-10 mt-1 w-full overflow-hidden rounded-md border bg-background shadow-md">
                {autocomplete.data!.slice(0, 6).map((hit) => (
                  <button
                    key={hit.id}
                    type="button"
                    className="block w-full px-3 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
                    onClick={() => {
                      setQuery(hit.title ?? "");
                      runSearch(hit.title ?? "");
                    }}
                  >
                    {hit.title}
                  </button>
                ))}
              </div>
            )}
          </div>
          {message && <p className="text-sm text-muted-foreground">{message}</p>}
        </CardContent>
      </Card>

      <section className="space-y-3">
        <div className="flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-primary" />
          <h2 className="text-lg font-semibold">Recommended for you</h2>
        </div>
        {recommendations.isLoading && <p className="text-sm text-muted-foreground">Loading…</p>}
        {recommendations.data && recommendations.data.length === 0 && (
          <p className="text-sm text-muted-foreground">
            Subscribe to or rate a travel to unlock personalized suggestions.
          </p>
        )}
        <CardGrid
          items={(recommendations.data ?? []).map((r) => ({
            id: r.id,
            title: r.title,
            subtitle: `${r.score} shared trait${r.score > 1 ? "s" : ""}`,
            badge: r.status ?? undefined,
          }))}
          onSubscribe={onSubscribe}
          pending={subscribe.isPending}
        />
      </section>

      {committed && (
        <section className="space-y-3">
          <h2 className="text-lg font-semibold">Results for “{committed}”</h2>
          {results.isLoading && <p className="text-sm text-muted-foreground">Searching…</p>}
          {results.data && results.data.length === 0 && (
            <p className="text-sm text-muted-foreground">No travels match your search.</p>
          )}
          <CardGrid
            items={(results.data ?? []).map((h) => ({
              id: h.id,
              title: h.title,
              subtitle: h.price != null ? `${h.price} ${h.currency ?? ""}` : undefined,
              badge: h.status ?? undefined,
            }))}
            onSubscribe={onSubscribe}
            pending={subscribe.isPending}
          />
        </section>
      )}
    </div>
  );
}

function CardGrid({
  items,
  onSubscribe,
  pending,
}: {
  items: CardData[];
  onSubscribe: (id: string, title: string | null) => void;
  pending: boolean;
}) {
  if (items.length === 0) return null;
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {items.map((item) => (
        <Card key={item.id} className="flex flex-col">
          <CardHeader className="flex-1">
            <div className="flex items-start justify-between gap-2">
              <CardTitle className="text-base">{item.title ?? "Untitled"}</CardTitle>
              {item.badge && <Badge variant="secondary">{item.badge}</Badge>}
            </div>
            {item.subtitle && (
              <p className="text-sm text-muted-foreground">{item.subtitle}</p>
            )}
          </CardHeader>
          <CardContent>
            <Button
              size="sm"
              className="w-full"
              disabled={pending}
              onClick={() => onSubscribe(item.id, item.title)}
            >
              {pending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Ticket className="mr-2 h-4 w-4" />
              )}
              Subscribe
            </Button>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
